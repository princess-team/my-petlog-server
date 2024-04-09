package com.ppp.api.diary.service;

import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.diary.validator.DiaryAccessValidator;
import com.ppp.api.notification.dto.event.DiaryNotificationEvent;
import com.ppp.api.user.dto.response.UserResponse;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.notification.constant.MessageCode;
import com.ppp.domain.user.User;
import com.ppp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ppp.api.diary.exception.ErrorCode.DIARY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DiaryLikeService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DiaryRedisService diaryRedisService;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DiaryAccessValidator diaryAccessValidator;

    @Transactional
    public void likeDiary(User user, Long petId, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        diaryAccessValidator.validateAccessDiary(petId, user.getId(), diary);
        if (diaryRedisService.isLikeExistByDiaryIdAndUserId(diaryId, user.getId()))
            diaryRedisService.cancelLikeByDiaryIdAndUserId(diaryId, user.getId());
        else {
            diaryRedisService.registerLikeByDiaryIdAndUserId(diaryId, user.getId());
            notifyDiaryLike(user, diary);
        }
    }

    private void notifyDiaryLike(User sender, Diary diary) {
        if (!sender.getId().equals(diary.getUser().getId()))
            applicationEventPublisher.publishEvent(new DiaryNotificationEvent(MessageCode.DIARY_LIKE, sender, diary));
    }


    public List<UserResponse> retrieveDiaryLikeUsers(User user, Long diaryId) {
        if (!diaryRepository.existsByIdAndIsDeletedFalse(diaryId))
            throw new DiaryException(DIARY_NOT_FOUND);
        Set<String> likedUserIds = diaryRedisService.getLikedUserIdsByDiaryId(diaryId);
        if (likedUserIds.isEmpty()) return new ArrayList<>();
        return userRepository.findByIdIn(likedUserIds)
                .stream().map(likedUser -> UserResponse.from(likedUser, user.getId()))
                .collect(Collectors.toList());
    }
}
