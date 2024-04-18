package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.event.DiaryCommentCreatedEvent;
import com.ppp.api.diary.dto.event.DiaryCommentDeletedEvent;
import com.ppp.api.diary.dto.event.DiaryReCommentCreatedEvent;
import com.ppp.api.diary.dto.request.DiaryCommentRequest;
import com.ppp.api.diary.dto.response.DiaryCommentResponse;
import com.ppp.api.diary.dto.response.DiaryReCommentResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.diary.validator.DiaryAccessValidator;
import com.ppp.api.notification.dto.event.DiaryNotificationEvent;
import com.ppp.api.notification.dto.event.DiaryReCommentNotificationEvent;
import com.ppp.api.notification.dto.event.DiaryTagNotificationEvent;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryComment;
import com.ppp.domain.diary.repository.DiaryCommentRepository;
import com.ppp.domain.diary.repository.DiaryRepository;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.ppp.api.diary.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryCommentService {

    private final DiaryCommentRepository diaryCommentRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DiaryCommentRedisService diaryCommentRedisService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DiaryAccessValidator diaryAccessValidator;

    @Transactional
    public DiaryCommentResponse createComment(User user, Long petId, Long diaryId, DiaryCommentRequest request) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .filter(foundDiary -> Objects.equals(foundDiary.getPet().getId(), petId))
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), diary);

        DiaryComment savedComment = diaryCommentRepository.save(DiaryComment.builder()
                .content(request.getContent())
                .taggedUsersIdNicknameMap(getTaggedUsersIdNicknameMap(petId, request.getTaggedUserIds()))
                .diary(diary)
                .user(user)
                .build());
        applicationEventPublisher.publishEvent(new DiaryCommentCreatedEvent(savedComment));
        notifyDiaryComment(diary, user, request.getTaggedUserIds());
        return DiaryCommentResponse.from(savedComment, user.getId());
    }

    private void notifyDiaryComment(Diary diary, User sender, List<String> taggedUserIds) {
        if (!sender.getId().equals(diary.getUser().getId()))
            applicationEventPublisher.publishEvent(new DiaryNotificationEvent(MessageCode.DIARY_COMMENT_CREATE, sender, diary));
        if (taggedUserIds != null && !taggedUserIds.isEmpty())
            applicationEventPublisher.publishEvent(new DiaryTagNotificationEvent(MessageCode.DIARY_TAG, sender, diary.getPet(), taggedUserIds));
    }

    private Map<String, String> getTaggedUsersIdNicknameMap(Long petId, List<String> taggedUsers) {
        if (taggedUsers.isEmpty()) return new HashMap<>();
        Map<String, String> taggedUsersIdNicknameMap = new HashMap<>();
        userRepository.findByGuardianUsersByPetIdAndUserIdsContaining(petId, taggedUsers)
                .forEach(taggedUser -> taggedUsersIdNicknameMap.put(taggedUser.getId(), taggedUser.getNickname()));
        return taggedUsersIdNicknameMap;
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
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), comment.getDiary());
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
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), diaryId);

        return diaryCommentRepository.findAncestorCommentByDiaryId(diaryId, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(comment -> toDiaryCommentResponse(comment, user.getId()));
    }

    private DiaryCommentResponse toDiaryCommentResponse(DiaryComment comment, String userId) {
        if (comment.isDeleted())
            return DiaryCommentResponse.ofDeletedComment(comment.getId(), diaryCommentRedisService.getDiaryReCommentCountByCommentId(comment.getId()));
        return DiaryCommentResponse.from(comment, userId,
                diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(comment.getId(), userId),
                diaryCommentRedisService.getLikeCountByCommentId(comment.getId()),
                diaryCommentRedisService.getDiaryReCommentCountByCommentId(comment.getId()));
    }

    public void likeComment(User user, Long petId, Long commentId) {
        DiaryComment comment = diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(commentId, petId)
                .orElseThrow(() -> new DiaryException(DIARY_COMMENT_NOT_FOUND));
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), comment.getDiary());

        if (diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(commentId, user.getId()))
            diaryCommentRedisService.cancelLikeByCommentIdAndUserId(commentId, user.getId());
        else
            diaryCommentRedisService.registerLikeByCommentIdAndUserId(commentId, user.getId());
    }

    @Transactional
    public DiaryReCommentResponse createReComment(User user, Long petId, Long commentId, DiaryCommentRequest request) {
        DiaryComment parentComment = diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(commentId, petId)
                .orElseThrow(() -> new DiaryException(DIARY_COMMENT_NOT_FOUND));
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), parentComment.getDiary());

        DiaryComment savedComment = diaryCommentRepository.save(DiaryComment.builder()
                .content(request.getContent())
                .taggedUsersIdNicknameMap(getTaggedUsersIdNicknameMap(petId, request.getTaggedUserIds()))
                .diary(parentComment.getDiary())
                .user(user)
                .parent(parentComment)
                .build());
        applicationEventPublisher.publishEvent(new DiaryReCommentCreatedEvent(savedComment));

        notifyDiaryReComment(parentComment, user, request.getTaggedUserIds());

        return DiaryReCommentResponse.from(savedComment, user.getId());
    }

    private void notifyDiaryReComment(DiaryComment parentComment, User sender, List<String> taggedUserIds) {
        User receiver = parentComment.getUser();
        if (!sender.getId().equals(receiver.getId()))
            applicationEventPublisher.publishEvent(new DiaryReCommentNotificationEvent(MessageCode.DIARY_RECOMMENT_CREATE, sender, receiver));
        if (taggedUserIds != null && !taggedUserIds.isEmpty()) {
            Optional<Diary> maybeDiary = diaryRepository.findByIdAndIsDeletedFalse(parentComment.getDiary().getId());
            maybeDiary.ifPresent(diary -> applicationEventPublisher.publishEvent(new DiaryTagNotificationEvent(MessageCode.DIARY_TAG, sender, diary.getPet(), taggedUserIds)));
        }
    }

    public List<DiaryReCommentResponse> displayReComments(User user, Long petId, Long diaryId, Long ancestorId) {
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), diaryId);
        return diaryCommentRepository.findByAncestorCommentIdAndIsDeletedFalseOrderByIdDesc(ancestorId).stream()
                .map(recomment -> DiaryReCommentResponse.from(recomment, user.getId(),
                        diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(recomment.getId(), user.getId()),
                        diaryCommentRedisService.getLikeCountByCommentId(recomment.getId())))
                .collect(Collectors.toList());
    }
}
