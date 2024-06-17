package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.event.DiaryCreatedEvent;
import com.ppp.api.diary.dto.event.DiaryDeletedEvent;
import com.ppp.api.diary.dto.event.DiaryUpdatedEvent;
import com.ppp.api.diary.dto.request.DiaryCreateRequest;
import com.ppp.api.diary.dto.request.DiaryUpdateRequest;
import com.ppp.api.diary.dto.response.DiaryDetailResponse;
import com.ppp.api.diary.dto.response.DiaryGroupByDateResponse;
import com.ppp.api.diary.dto.response.DiaryResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.diary.validator.DiaryAccessValidator;
import com.ppp.api.pet.exception.PetException;
import com.ppp.api.video.exception.ErrorCode;
import com.ppp.api.video.exception.VideoException;
import com.ppp.common.service.FileStorageManageService;
import com.ppp.common.service.ThumbnailService;
import com.ppp.domain.common.constant.FileType;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryMedia;
import com.ppp.domain.diary.constant.DiaryMediaType;
import com.ppp.domain.diary.constant.DiaryPolicy;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.repository.PetRepository;
import com.ppp.domain.user.User;
import com.ppp.domain.video.TempVideo;
import com.ppp.domain.video.repository.TempVideoRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ppp.api.diary.exception.ErrorCode.*;
import static com.ppp.api.pet.exception.ErrorCode.PET_NOT_FOUND;
import static com.ppp.domain.common.constant.Domain.DIARY;
import static com.ppp.domain.diary.constant.DiaryPolicy.DEFAULT_THUMBNAIL_PATH;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final PetRepository petRepository;
    private final GuardianRepository guardianRepository;
    private final FileStorageManageService fileStorageManageService;
    private final DiaryCommentRedisService diaryCommentRedisService;
    private final DiaryRedisService diaryRedisService;
    private final ThumbnailService thumbnailService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TempVideoRedisRepository tempVideoRedisRepository;
    private final DiaryAccessValidator diaryAccessValidator;

    @Transactional
    public void createDiary(User user, Long petId, DiaryCreateRequest request, List<MultipartFile> images) {
        Pet pet = petRepository.findByIdAndIsDeletedFalse(petId)
                .orElseThrow(() -> new PetException(PET_NOT_FOUND));
        validateWriteDiary(petId, user);
        List<TempVideo> uploadedVideos = getUploadedVideos(request.getUploadedVideoIds(), user);

        Diary diary = Diary.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isPublic(request.getIsPublic())
                .date(LocalDate.parse(request.getDate()))
                .user(user)
                .pet(pet)
                .build();
        diary.addDiaryMedias(uploadAndGetDiaryMedias(images, uploadedVideos, diary));

        applicationEventPublisher.publishEvent(
                new DiaryCreatedEvent(diaryRepository.save(diary).getId()));
    }

    private void validateWriteDiary(Long petId, User user) {
        if (!guardianRepository.existsByUserIdAndPetId(user.getId(), petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
    }

    private List<TempVideo> getUploadedVideos(List<String> videoIds, User user) {
        if (videoIds.isEmpty())
            return new ArrayList<>();
        return videoIds.stream().map(videoId ->
                tempVideoRedisRepository.findById(videoId).stream()
                        .filter(video -> Objects.equals(video.getUserId(), user.getId())).findFirst()
                        .orElseThrow(() -> new VideoException(ErrorCode.NOT_FOUND_VIDEO))).toList();
    }

    private List<DiaryMedia> uploadAndGetDiaryMedias(List<MultipartFile> images, List<TempVideo> tempVideos, Diary diary) {
        List<DiaryMedia> diaryMedias = uploadImagesIfNeeded(images, diary);
        uploadVideoIfNeeded(diaryMedias, tempVideos, diary);
        return diaryMedias;
    }

    private List<DiaryMedia> uploadImagesIfNeeded(List<MultipartFile> images, Diary diary) {
        if (images == null || images.isEmpty())
            return new ArrayList<>();
        return fileStorageManageService.uploadImages(images, DIARY).stream()
                .map(uploadedPath -> DiaryMedia.of(diary, uploadedPath, DiaryMediaType.IMAGE))
                .collect(Collectors.toList());
    }

    private void uploadVideoIfNeeded(List<DiaryMedia> diaryMedias, List<TempVideo> tempVideos, Diary diary) {
        if (tempVideos.isEmpty())
            return;
        fileStorageManageService.uploadVideos(tempVideos, DIARY)
                .forEach(uploadedPath -> diaryMedias.add(DiaryMedia.of(diary, uploadedPath, DiaryMediaType.VIDEO)));
    }

    @Transactional
    public void updateDiary(User user, Long petId, Long diaryId, DiaryUpdateRequest request, List<MultipartFile> images) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .filter(foundDiary -> Objects.equals(foundDiary.getPet().getId(), petId))
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        validateModifyDiary(diary, user, petId);
        List<DiaryMedia> keepingVideos = diary.getKeepingVideos(request.getDeletedVideoIds());
        validateVideoSize(keepingVideos.size(), request.getUploadedVideoIds().size());
        List<TempVideo> newlyUploadedVideos = getUploadedVideos(request.getUploadedVideoIds(), user);

        List<DiaryMedia> diaryMediasToBeDeleted = getDiaryMediasToBeDeleted(diary, keepingVideos);
        List<DiaryMedia> diaryMediasToBeUpdated = uploadAndGetDiaryMedias(images, newlyUploadedVideos, diary);
        keepOldDiaryMedia(diaryMediasToBeUpdated, keepingVideos);

        applicationEventPublisher.publishEvent(new DiaryUpdatedEvent(diaryId, diaryMediasToBeDeleted, diary.getThumbnailPath()));
        diary.update(request.getTitle(), request.getContent(), LocalDate.parse(request.getDate()), diaryMediasToBeUpdated, request.getIsPublic());
    }

    private List<DiaryMedia> getDiaryMediasToBeDeleted(Diary diary, List<DiaryMedia> keepingVideos) {
        List<DiaryMedia> diaryMediasToBeDeleted = new ArrayList<>(diary.getDiaryMedias());
        diaryMediasToBeDeleted.removeAll(keepingVideos);
        return diaryMediasToBeDeleted;
    }

    private void keepOldDiaryMedia(List<DiaryMedia> updatedMedias, List<DiaryMedia> keepingVideos) {
        updatedMedias.addAll(keepingVideos);
    }

    private void validateVideoSize(int keepingVideoSize, int requestedVideoSize) {
        if (keepingVideoSize + requestedVideoSize > DiaryPolicy.VIDEO_UPLOAD_LIMIT)
            throw new DiaryException(MEDIA_UPLOAD_LIMIT_OVER);
    }

    private void validateModifyDiary(Diary diary, User user, Long petId) {
        if (!Objects.equals(diary.getUser().getId(), user.getId()))
            throw new DiaryException(NOT_DIARY_OWNER);
        validateWriteDiary(petId, user);
    }


    @Transactional
    public void deleteDiary(User user, Long petId, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .filter(foundDiary -> Objects.equals(foundDiary.getPet().getId(), petId))
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        validateModifyDiary(diary, user, petId);

        applicationEventPublisher.publishEvent(new DiaryDeletedEvent(diaryId,
                new ArrayList<>(diary.getDiaryMedias()), diary.getThumbnailPath()));
        diary.delete();
    }

    public DiaryDetailResponse displayDiary(User user, Long petId, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .filter(foundDiary -> Objects.equals(foundDiary.getPet().getId(), petId))
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), diary);
        return DiaryDetailResponse.from(diary, user.getId(),
                diaryCommentRedisService.getDiaryCommentCountByDiaryId(diaryId),
                diaryRedisService.isLikeExistByDiaryIdAndUserId(diaryId, user.getId()),
                diaryRedisService.getLikeCountByDiaryId(diaryId));
    }

    public Slice<DiaryGroupByDateResponse> displayDiaries(User user, Long petId, int page, int size) {
        return getGroupedDiariesSlice(
                diaryRepository.findByPetIdAndIsDeletedFalseAndIsPublicInOrderByDateDesc(
                        petId, getUsersDiaryViewingRange(user, petId),
                        PageRequest.of(page, size)), user.getId());
    }

    private Set<Boolean> getUsersDiaryViewingRange(User user, Long petId) {
        return new HashSet<>(List.of(true, !guardianRepository.existsByUserIdAndPetId(user.getId(), petId)));
    }

    private Slice<DiaryGroupByDateResponse> getGroupedDiariesSlice(Slice<Diary> diarySlice, String userId) {
        if (diarySlice.getContent().isEmpty())
            return new SliceImpl<>(new ArrayList<>(), diarySlice.getPageable(), diarySlice.hasNext());

        List<DiaryGroupByDateResponse> content = new ArrayList<>();
        List<DiaryResponse> sameDaysDiaries = new ArrayList<>();
        LocalDate prevDate = diarySlice.getContent().get(0).getDate();
        for (Diary diary : diarySlice.getContent()) {
            if (!prevDate.equals(diary.getDate())) {
                content.add(DiaryGroupByDateResponse.of(prevDate, sameDaysDiaries));
                prevDate = diary.getDate();
                sameDaysDiaries = new ArrayList<>();
            }
            sameDaysDiaries.add(
                    DiaryResponse.from(diary, userId,
                            diaryCommentRedisService.getDiaryCommentCountByDiaryId(diary.getId())));
        }
        content.add(DiaryGroupByDateResponse.of(prevDate, sameDaysDiaries));

        return new SliceImpl<>(content, diarySlice.getPageable(), diarySlice.hasNext());
    }

    @Transactional
    public Diary saveThumbnail(Long diaryId) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        List<DiaryMedia> diaryMedias = diary.getDiaryMedias();
        if (diaryMedias.isEmpty()) {
            diary.addThumbnail(DiaryPolicy.DEFAULT_THUMBNAIL_PATH);
        } else {
            diary.addThumbnail(getThumbnailFromDiaryMedia(diaryMedias.get(0)));
        }
        return diary;
    }

    public String getThumbnailFromDiaryMedia(DiaryMedia thumbnailMedia) {
        try {
            if (DiaryMediaType.IMAGE.equals(thumbnailMedia.getType()))
                return thumbnailService.uploadThumbnailFromStorageFile(thumbnailMedia.getPath(), FileType.IMAGE, DIARY);
            return thumbnailService.uploadThumbnailFromStorageFile(thumbnailMedia.getPath(), FileType.VIDEO, DIARY);
        } catch (Exception e) {
            return DiaryPolicy.DEFAULT_THUMBNAIL_PATH;
        }
    }

    @Transactional
    public void deleteAllByPetId(Long petId) {
        List<String> deletedPaths = new ArrayList<>();
        diaryRepository.findByPetIdAndIsDeletedFalse(petId)
                .forEach(diary -> {
                    diary.delete();
                    deletedPaths.addAll(diary.getDiaryMedias().stream().map(DiaryMedia::getPath).toList());
                    if (diary.getThumbnailPath() != null && !Objects.equals(diary.getThumbnailPath(), DEFAULT_THUMBNAIL_PATH))
                        deletedPaths.add(diary.getThumbnailPath());
                });
        fileStorageManageService.deleteImages(deletedPaths);
    }

    public DiaryDetailResponse displayFeedDiary(User user, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        return DiaryDetailResponse.from(diary, user.getId(),
                diaryCommentRedisService.getDiaryCommentCountByDiaryId(diaryId),
                diaryRedisService.isLikeExistByDiaryIdAndUserId(diaryId, user.getId()),
                diaryRedisService.getLikeCountByDiaryId(diaryId));
    }
}
