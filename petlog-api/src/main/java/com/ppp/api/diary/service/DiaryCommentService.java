package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.event.DiaryCommentCreatedEvent;
import com.ppp.api.diary.dto.event.DiaryCommentDeletedEvent;
import com.ppp.api.diary.dto.event.DiaryReCommentCreatedEvent;
import com.ppp.api.diary.dto.request.DiaryCommentRequest;
import com.ppp.api.diary.dto.response.DiaryCommentResponse;
import com.ppp.api.diary.dto.response.DiaryReCommentResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.notification.dto.event.DiaryNotificationEvent;
import com.ppp.api.notification.dto.event.DiaryTagNotificationEvent;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryComment;
import com.ppp.domain.diary.repository.DiaryCommentRepository;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.notification.constant.MessageCode;
import com.ppp.domain.user.User;
import com.ppp.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ppp.api.diary.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryCommentService {

    private final DiaryCommentRepository diaryCommentRepository;
    private final DiaryRepository diaryRepository;
    private final GuardianRepository guardianRepository;
    private final UserRepository userRepository;
    private final DiaryCommentRedisService diaryCommentRedisService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DiaryCommentResponse createComment(User user, Long petId, Long diaryId, DiaryCommentRequest request) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .filter(foundDiary -> Objects.equals(foundDiary.getPet().getId(), petId))
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        validateCreateComment(petId, user);

        DiaryComment savedComment = diaryCommentRepository.save(DiaryComment.builder()
                .content(request.getContent())
                .taggedUsersIdNicknameMap(getTaggedUsersIdNicknameMap(petId, request.getTaggedUserIds()))
                .diary(diary)
                .user(user)
                .build());
        applicationEventPublisher.publishEvent(new DiaryCommentCreatedEvent(savedComment));
        if (!user.getId().equals(diary.getUser().getId())) applicationEventPublisher.publishEvent(new DiaryNotificationEvent(MessageCode.DIARY_COMMENT_CREATE, user, diary));
        if (request.getTaggedUserIds() != null && !request.getTaggedUserIds().isEmpty()) applicationEventPublisher.publishEvent(new DiaryTagNotificationEvent(MessageCode.DIARY_TAG, user, diary, request.getTaggedUserIds()));

        return DiaryCommentResponse.from(savedComment, user.getId());
    }

    private Map<String, String> getTaggedUsersIdNicknameMap(Long petId, List<String> taggedUsers) {
        if (taggedUsers.isEmpty()) return new HashMap<>();
        Map<String, String> taggedUsersIdNicknameMap = new HashMap<>();
        userRepository.findByGuardianUsersByPetIdAndUserIdsContaining(petId, taggedUsers)
                .forEach(taggedUser -> taggedUsersIdNicknameMap.put(taggedUser.getId(), taggedUser.getNickname()));
        return taggedUsersIdNicknameMap;
    }

    private void validateCreateComment(Long petId, User user) {
        if (!guardianRepository.existsByUserIdAndPetId(user.getId(), petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
    }

    @Transactional
    public void updateComment(User user, Long petId, Long commentId, DiaryCommentRequest request) {
        DiaryComment comment = diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(commentId, petId)
                .orElseThrow(() -> new DiaryException(DIARY_COMMENT_NOT_FOUND));
        validateModifyComment(comment, user, petId);

        comment.update(request.getContent(), getTaggedUsersIdNicknameMap(petId, request.getTaggedUserIds()));
    }

    private void validateModifyComment(DiaryComment comment, User user, Long petId) {
        if (!Objects.equals(comment.getUser().getId(), user.getId()))
            throw new DiaryException(NOT_DIARY_COMMENT_OWNER);
        if (!guardianRepository.existsByUserIdAndPetId(user.getId(), petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
    }

    @Transactional
    public void deleteComment(User user, Long petId, Long commentId) {
        DiaryComment comment = diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(commentId, petId)
                .orElseThrow(() -> new DiaryException(DIARY_COMMENT_NOT_FOUND));
        validateModifyComment(comment, user, petId);

        comment.delete();
        applicationEventPublisher.publishEvent(new DiaryCommentDeletedEvent(comment));
    }

    public Slice<DiaryCommentResponse> displayComments(User user, Long petId, Long diaryId, int page, int size) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .filter(foundDiary -> Objects.equals(foundDiary.getPet().getId(), petId))
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        validateDisplayComments(user, petId);

        return diaryCommentRepository.findByDiaryAndAncestorCommentIdIsNullAndIsDeletedFalse(diary, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(comment -> DiaryCommentResponse.from(comment, user.getId(),
                        diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(comment.getId(), user.getId()),
                        diaryCommentRedisService.getLikeCountByCommentId(comment.getId()),
                        diaryCommentRedisService.getDiaryReCommentCountByCommentId(comment.getId())));
    }

    private void validateDisplayComments(User user, Long petId) {
        if (!guardianRepository.existsByUserIdAndPetId(user.getId(), petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
    }

    public void likeComment(User user, Long petId, Long commentId) {
        validateLikeComment(user, petId, commentId);

        if (diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(commentId, user.getId()))
            diaryCommentRedisService.cancelLikeByCommentIdAndUserId(commentId, user.getId());
        else
            diaryCommentRedisService.registerLikeByCommentIdAndUserId(commentId, user.getId());
    }

    private void validateLikeComment(User user, Long petId, Long commentId) {
        if (!diaryCommentRepository.existsByIdAndIsDeletedFalse(commentId))
            throw new DiaryException(DIARY_COMMENT_NOT_FOUND);
        if (!guardianRepository.existsByUserIdAndPetId(user.getId(), petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
    }

    @Transactional
    public DiaryReCommentResponse createReComment(User user, Long petId, Long commentId, DiaryCommentRequest request) {
        DiaryComment parentComment = diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(commentId, petId)
                .orElseThrow(() -> new DiaryException(DIARY_COMMENT_NOT_FOUND));
        validateCreateComment(petId, user);

        DiaryComment savedComment = diaryCommentRepository.save(DiaryComment.builder()
                .content(request.getContent())
                .taggedUsersIdNicknameMap(getTaggedUsersIdNicknameMap(petId, request.getTaggedUserIds()))
                .diary(parentComment.getDiary())
                .user(user)
                .parent(parentComment)
                .build());
        applicationEventPublisher.publishEvent(new DiaryReCommentCreatedEvent(savedComment));
        return DiaryReCommentResponse.from(savedComment, user.getId());
    }

    public List<DiaryReCommentResponse> displayReComments(User user, Long petId, Long ancestorId) {
        validateDisplayComments(user, petId);

        return diaryCommentRepository.findByAncestorCommentIdAndIsDeletedFalseOrderByIdDesc(ancestorId).stream()
                .map(recomment -> DiaryReCommentResponse.from(recomment, user.getId(),
                        diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(recomment.getId(), user.getId()),
                        diaryCommentRedisService.getLikeCountByCommentId(recomment.getId())))
                .collect(Collectors.toList());
    }
}
