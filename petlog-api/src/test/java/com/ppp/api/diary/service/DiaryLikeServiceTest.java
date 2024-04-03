package com.ppp.api.diary.service;

import com.ppp.api.diary.exception.DiaryException;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.ppp.api.diary.exception.ErrorCode.DIARY_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiaryLikeServiceTest {
    @Mock
    private DiaryRepository diaryRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private DiaryRedisService diaryRedisService;
    @InjectMocks
    private DiaryLikeService diaryLikeService;

    User user = User.builder()
            .id("randomstring")
            .nickname("hi")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .build();

    User userA = User.builder()
            .id("cherrymommy")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .nickname("체리엄마")
            .build();

    Pet pet = Pet.builder()
            .id(1L)
            .birth(LocalDateTime.of(2023, 2, 8, 0, 0))
            .build();

    Diary diary = Diary.builder()
            .title("우리집 고양이")
            .isPublic(true)
            .content("츄르를 좋아해")
            .date(LocalDate.of(2020, 11, 11))
            .user(user)
            .pet(pet).build();

    @Test
    @DisplayName("다이어리 좋아요 성공")
    void likeDiary_success() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(diaryRedisService.isLikeExistByDiaryIdAndUserId(anyLong(), anyString()))
                .willReturn(false);
        //when
        diaryLikeService.likeDiary(userA, 1L, 2L);
        //then
        verify(diaryRedisService, times(1)).registerLikeByDiaryIdAndUserId(anyLong(), anyString());
    }

    @Test
    @DisplayName("다이어리 좋아요 성공-취소할때")
    void likeDiary_success_WhenCancelCase() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(diaryRedisService.isLikeExistByDiaryIdAndUserId(anyLong(), anyString()))
                .willReturn(true);
        //when
        diaryLikeService.likeDiary(userA, 1L, 2L);
        //then
        verify(diaryRedisService, times(1)).cancelLikeByDiaryIdAndUserId(anyLong(), anyString());
    }

    @Test
    @DisplayName("다이어리 좋아요 실패-DIARY_NOT_FOUND")
    void likeDiary_fail_DIARY_NOT_FOUND() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryLikeService.likeDiary(userA, 1L, 2L));
        //then
        assertEquals(DIARY_NOT_FOUND.getCode(), exception.getCode());
    }
}