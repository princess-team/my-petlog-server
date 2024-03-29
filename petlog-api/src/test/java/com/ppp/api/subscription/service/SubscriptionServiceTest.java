package com.ppp.api.subscription.service;

import com.ppp.api.subscription.dto.response.SubscribedPetResponse;
import com.ppp.api.subscription.dto.response.SubscriberResponse;
import com.ppp.api.subscription.dto.transfer.SubscriptionInfoDto;
import com.ppp.api.subscription.exception.ErrorCode;
import com.ppp.api.subscription.exception.SubscriptionException;
import com.ppp.common.service.CacheManageService;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.dto.PetDto;
import com.ppp.domain.pet.repository.PetQuerydslRepository;
import com.ppp.domain.pet.repository.PetRepository;
import com.ppp.domain.subscription.Subscription;
import com.ppp.domain.subscription.constant.Status;
import com.ppp.domain.subscription.repository.SubscriptionRepository;
import com.ppp.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private PetQuerydslRepository petQuerydslRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private CacheManageService cacheManageService;
    @InjectMocks
    private SubscriptionService subscriptionService;

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

    @Test
    @DisplayName("구독 및 구독 해제 성공-구독 해제")
    void subscribeOrUnsubscribe_success_WhenUnsubscribe() {
        //given
        given(petRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(pet));
        Subscription subscription = Subscription.builder()
                .subscriber(user)
                .pet(pet)
                .build();
        given(subscriptionRepository.findBySubscriberAndPet(any(), any()))
                .willReturn(Optional.of(subscription));
        //when
        subscriptionService.subscribeOrUnsubscribe(1L, user);
        //then
        verify(subscriptionRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("구독 및 구독 해제 성공-구독")
    void subscribeOrUnsubscribe_success_WhenSubscribe() {
        //given
        Pet pet = Pet.builder()
                .user(user)
                .id(1L).build();
        given(petRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(pet));
        given(subscriptionRepository.findBySubscriberAndPet(any(), any()))
                .willReturn(Optional.empty());
        //when
        subscriptionService.subscribeOrUnsubscribe(1L, user);
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        //then
        verify(subscriptionRepository, times(1)).save(captor.capture());
        Subscription saved = captor.getValue();
        assertEquals(1L, saved.getPet().getId());
        assertEquals("abcde1234", saved.getSubscriber().getId());
    }

    @Test
    @DisplayName("구독중인 펫 계정 조회")
    void displayMySubscribingPets_success() {
        //given
        given(petQuerydslRepository.findSubscribedPetsByUserId(anyString()))
                .willReturn(List.of(new PetDto(1L, "PET/111111111/1232132313dsfadskfakfsa.jpg", "강아지강씨"),
                        new PetDto(2L, "PET/12345678/1232132313dsfadskfakfsa.jpg", "고양이고씨")));
        //when
        List<SubscribedPetResponse> responses = subscriptionService.displayMySubscribedPets(user);
        //then
        assertEquals(1, responses.get(0).id());
        assertEquals("강아지강씨", responses.get(0).name());
        assertEquals("PET/111111111/1232132313dsfadskfakfsa.jpg", responses.get(0).profilePath());
        assertEquals(2, responses.get(1).id());
        assertEquals("고양이고씨", responses.get(1).name());
        assertEquals("PET/12345678/1232132313dsfadskfakfsa.jpg", responses.get(1).profilePath());
    }

    @Test
    @DisplayName("구독자 리스트 조회")
    void displayMyPetsSubscribers_success() {
        //given
        given(petRepository.existsByIdAndUserIdAndIsDeletedFalse(anyLong(), anyString()))
                .willReturn(true);
        given(subscriptionRepository.findByPetId(anyLong()))
                .willReturn(List.of(Subscription.builder()
                                .subscriber(user)
                                .pet(pet).build(),
                        Subscription.builder()
                                .subscriber(userA)
                                .pet(pet).build()
                ));
        //when
        List<SubscriberResponse> responses = subscriptionService.displayMyPetsSubscribers(1L, user);
        //then
        assertEquals("abcde1234", responses.get(0).id());
        assertEquals("hi", responses.get(0).nickname());
        assertEquals("USER/12345678/1232132313dsfadskfakfsa.jpg", responses.get(0).profilePath());
        assertEquals("abc123", responses.get(1).id());
        assertEquals("첫째누나", responses.get(1).nickname());
        assertEquals("USER/12345678/1232132313dsfadskfakfsa.jpg", responses.get(1).profilePath());
    }

    @Test
    @DisplayName("구독자 리스트 조회 실패-FORBIDDEN_PET_SPACE")
    void displayMyPetsSubscribers_fail_FORBIDDEN_PET_SPACE() {
        //given
        given(petRepository.existsByIdAndUserIdAndIsDeletedFalse(anyLong(), anyString()))
                .willReturn(false);
        //when
        SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.displayMyPetsSubscribers(1L, user));
        //then
        assertEquals(ErrorCode.FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("유저의 구독 정보 조회 테스트")
    void getUsersSubscriptionInfo_success() {
        //given
        given(subscriptionRepository.findBySubscriberId(anyString()))
                .willReturn(List.of(
                        Subscription.builder()
                                .status(Status.ACTIVE)
                                .subscriber(user)
                                .pet(pet).build(),
                        Subscription.builder()
                                .status(Status.BLOCK)
                                .subscriber(user)
                                .pet(petB).build()
                ));
        //when
        SubscriptionInfoDto response = subscriptionService.getUsersSubscriptionInfo("hihi");
        //then
        assertEquals(response.blockedPetIds().size(), 1);
        assertEquals(response.subscribedPetIds().size(), 1);
        assertTrue(response.subscribedPetIds().contains(1L));
        assertTrue(response.blockedPetIds().contains(2L));
    }

    @Test
    @DisplayName("구독자 차단 성공")
    void blockOrUnblockSubscriber_success_block() {
        //given
        given(petRepository.existsByIdAndUserIdAndIsDeletedFalse(anyLong(), anyString()))
                .willReturn(true);
        Subscription subscription = Subscription.builder()
                .status(Status.ACTIVE)
                .subscriber(user)
                .pet(pet).build();
        given(subscriptionRepository.findBySubscriberIdAndPetId(anyString(), anyLong()))
                .willReturn(Optional.of(subscription));
        //when
        subscriptionService.blockOrUnblockSubscriber(1L, "abcd1234", user);
        //then
        assertTrue(subscription.isBlocked());
        verify(cacheManageService, times(1)).deleteCachedSubscriptionInfo(anyString());
    }

    @Test
    @DisplayName("구독자 차단 성공-차단 해제")
    void blockOrUnblockSubscriber_success_unblock() {
        //given
        given(petRepository.existsByIdAndUserIdAndIsDeletedFalse(anyLong(), anyString()))
                .willReturn(true);
        Subscription subscription = Subscription.builder()
                .status(Status.BLOCK)
                .subscriber(user)
                .pet(pet).build();
        given(subscriptionRepository.findBySubscriberIdAndPetId(anyString(), anyLong()))
                .willReturn(Optional.of(subscription));
        //when
        subscriptionService.blockOrUnblockSubscriber(1L, "abcd1234", user);
        //then
        assertFalse(subscription.isBlocked());
        verify(cacheManageService, times(1)).deleteCachedSubscriptionInfo(anyString());
    }

    @Test
    @DisplayName("구독자 차단 실패-FORBIDDEN_PET_SPACE")
    void blockOrUnblockSubscriber_fail_FORBIDDEN_PET_SPACE() {
        //given
        given(petRepository.existsByIdAndUserIdAndIsDeletedFalse(anyLong(), anyString()))
                .willReturn(false);
        //when
        SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.blockOrUnblockSubscriber(1L, "abcd1234", user));
        //then
        assertEquals(ErrorCode.FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("구독자 차단 실패-SUBSCRIBER_NOT_FOUND")
    void blockOrUnblockSubscriber_fail_SUBSCRIBER_NOT_FOUND() {
        //given
        given(petRepository.existsByIdAndUserIdAndIsDeletedFalse(anyLong(), anyString()))
                .willReturn(true);
        given(subscriptionRepository.findBySubscriberIdAndPetId(anyString(), anyLong()))
                .willReturn(Optional.empty());
        //when
        SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.blockOrUnblockSubscriber(1L, "abcd1234", user));
        //then
        assertEquals(ErrorCode.SUBSCRIBER_NOT_FOUND.getCode(), exception.getCode());
    }
}