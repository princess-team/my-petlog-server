package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.event.DiaryDraftDeletedEvent;
import com.ppp.api.diary.dto.event.DiaryDraftUpdatedEvent;
import com.ppp.api.diary.dto.request.DiaryDraftCreateRequest;
import com.ppp.api.diary.dto.response.DiaryDraftCheckResponse;
import com.ppp.api.diary.dto.response.DiaryDraftResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.video.exception.ErrorCode;
import com.ppp.api.video.exception.VideoException;
import com.ppp.common.service.FileStorageManageService;
import com.ppp.domain.common.constant.Domain;
import com.ppp.domain.diary.DiaryDraft;
import com.ppp.domain.diary.repository.DiaryDraftRedisRepository;
import com.ppp.domain.user.User;
import com.ppp.domain.video.TempVideo;
import com.ppp.domain.video.repository.TempVideoRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ppp.api.diary.exception.ErrorCode.DIARY_DRAFT_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class DiaryDraftService {
    private final DiaryDraftRedisRepository diaryDraftRedisRepository;
    private final FileStorageManageService fileStorageManageService;
    private final TempVideoRedisRepository tempVideoRedisRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void createDiaryDraft(Long petId, DiaryDraftCreateRequest request, List<MultipartFile> images, User user) {
        Optional<DiaryDraft> maybeDiaryDraft = diaryDraftRedisRepository.findByPetIdAndUserId(petId, user.getId());
        if (maybeDiaryDraft.isEmpty())
            diaryDraftRedisRepository.save(
                    DiaryDraft.builder()
                            .title(request.getTitle())
                            .userId(user.getId())
                            .date(request.getDate())
                            .content(request.getContent())
                            .petId(petId)
                            .isPublic(request.getIsPublic())
                            .images(uploadImagesIfNeeded(images))
                            .videos(uploadVideosIfNeeded(request.getUploadedVideoIds(), user))
                            .build());
        else {
            DiaryDraft diaryDraftToBeUpdated = maybeDiaryDraft.get();
            applicationEventPublisher.publishEvent(new DiaryDraftUpdatedEvent(diaryDraftToBeUpdated.getMedias()));
            diaryDraftToBeUpdated.update(request.getTitle(), request.getContent(), request.getDate(), request.getIsPublic(),
                    uploadImagesIfNeeded(images), uploadVideosIfNeeded(request.getUploadedVideoIds(), user));
            diaryDraftRedisRepository.save(diaryDraftToBeUpdated);
        }
    }

    public List<String> uploadImagesIfNeeded(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return new ArrayList<>();
        return fileStorageManageService.uploadImages(images, Domain.DIARY_DRAFT);
    }

    public List<String> uploadVideosIfNeeded(List<String> videoIds, User user) {
        if (videoIds.isEmpty()) return new ArrayList<>();
        List<TempVideo> tempVideos = videoIds.stream().map(videoId ->
                tempVideoRedisRepository.findById(videoId).stream()
                        .filter(video -> Objects.equals(video.getUserId(), user.getId())).findFirst()
                        .orElseThrow(() -> new VideoException(ErrorCode.NOT_FOUND_VIDEO))).toList();
        return fileStorageManageService.uploadVideos(tempVideos, Domain.DIARY_DRAFT);
    }

    public DiaryDraftCheckResponse checkHasDiaryDraft(Long petId, User user) {
        return DiaryDraftCheckResponse.of(diaryDraftRedisRepository.existsByPetIdAndUserId(petId, user.getId()));
    }

    public DiaryDraftResponse retrieveDiaryDraft(Long petId, User user) {
        return DiaryDraftResponse.from(
                diaryDraftRedisRepository.findByPetIdAndUserId(petId, user.getId())
                        .orElseThrow(() -> new DiaryException(DIARY_DRAFT_NOT_FOUND)));
    }

    public void deleteDiaryDraft(Long petId, User user) {
        DiaryDraft diaryDraftToBeDeleted = diaryDraftRedisRepository.findByPetIdAndUserId(petId, user.getId())
                .orElseThrow(() -> new DiaryException(DIARY_DRAFT_NOT_FOUND));
        diaryDraftRedisRepository.delete(diaryDraftToBeDeleted);
        applicationEventPublisher.publishEvent(new DiaryDraftDeletedEvent(diaryDraftToBeDeleted.getMedias()));
    }

}
