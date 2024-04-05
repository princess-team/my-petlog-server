package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.response.DiaryFeedResponse;
import com.ppp.api.subscription.dto.transfer.SubscriptionInfoDto;
import com.ppp.api.subscription.service.SubscriptionService;
import com.ppp.common.security.PrincipalDetails;
import com.ppp.common.util.TimeUtil;
import com.ppp.domain.diary.dto.PetDiaryDto;
import com.ppp.domain.diary.repository.DiaryQuerydslRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DiaryFeedServiceTest {
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private DiaryQuerydslRepository diaryQuerydslRepository;
    @Mock
    private DiaryCommentRedisService diaryCommentRedisService;
    @Mock
    private DiaryRedisService diaryRedisService;
    @InjectMocks
    private DiaryFeedService diaryFeedService;

    static User user = User.builder()
            .id("abcde1234")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .nickname("hi")
            .build();

    static User userA = User.builder()
            .id("abc123")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .nickname("첫째누나")
            .build();

    static Pet pet = Pet.builder()
            .id(1L).build();

    static Pet petB = Pet.builder()
            .id(2L).build();

    static MockedStatic<TimeUtil> mockTimeUtil = mockStatic(TimeUtil.class);

    @BeforeAll
    static void init() {
        mockTimeUtil.when(() -> TimeUtil.calculateTerm(any())).thenReturn("2분");
    }

    @Test
    @DisplayName("피드 조회 성공")
    void retrieveDiaryFeed_success() {
        //given
        given(subscriptionService.getUsersSubscriptionInfo(anyString()))
                .willReturn(SubscriptionInfoDto.builder()
                        .subscribedPetIds(Set.of(1L))
                        .blockedPetIds(Set.of(2L))
                        .build());
        given(diaryQuerydslRepository.findSubscribedPetsDiariesBySubscription(anySet(), any()))
                .willReturn(List.of(
                        new PetDiaryDto(5L, 3L, "마루", new HashSet<>(), "/PET/profilepath", "나 사람됐다 짱이지", "마루는 네살", LocalDateTime.MIN)
                ));
        given(diaryCommentRedisService.getDiaryCommentCountByDiaryId(anyLong())).willReturn(0);
        given(diaryRedisService.isLikeExistByDiaryIdAndUserId(anyLong(), anyString())).willReturn(true);
        given(diaryRedisService.getLikeCountByDiaryId(anyLong())).willReturn(1);
        given(diaryQuerydslRepository.findRandomPetsDiaries(anySet(), any()))
                .willReturn(List.of(
                        new PetDiaryDto(5L, 3L, "마루", new HashSet<>(), "/PET/profilepath", "나 사람됐다 짱이지", "마루는 네살", LocalDateTime.MIN)
                ));
        //when
        Set<DiaryFeedResponse> responses = diaryFeedService.retrieveDiaryFeed(Optional.of(new PrincipalDetails(userA)), 0, 10);
        //then
        DiaryFeedResponse element = (DiaryFeedResponse) responses.toArray()[0];
        assertEquals(responses.size(), 2);
        assertEquals(element.content(), "나 사람됐다 짱이지");
        assertEquals(element.title(), "마루는 네살");
        assertEquals(element.pet().name(), "마루");
        assertEquals(element.diaryId(), 5L);
        assertTrue(element.isCurrentUserLiked());
    }


}