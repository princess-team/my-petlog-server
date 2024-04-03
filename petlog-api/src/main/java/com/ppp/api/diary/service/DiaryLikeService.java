package com.ppp.api.diary.service;

import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.notification.dto.event.DiaryNotificationEvent;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.notification.constant.MessageCode;
import com.ppp.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ppp.api.diary.exception.ErrorCode.DIARY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DiaryLikeService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DiaryRedisService diaryRedisService;
    private final DiaryRepository diaryRepository;

    @Transactional
    public void likeDiary(User user, Long petId, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndIsDeletedFalse(diaryId)
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));

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

}
