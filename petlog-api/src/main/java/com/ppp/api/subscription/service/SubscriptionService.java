package com.ppp.api.subscription.service;

import com.ppp.api.notification.dto.event.SubscribedNotificationEvent;
import com.ppp.api.pet.exception.ErrorCode;
import com.ppp.api.pet.exception.PetException;
import com.ppp.api.subscription.dto.response.SubscribedPetResponse;
import com.ppp.api.subscription.dto.response.SubscriberResponse;
import com.ppp.api.subscription.dto.transfer.SubscriptionInfoDto;
import com.ppp.api.subscription.exception.SubscriptionException;
import com.ppp.common.service.CacheManageService;
import com.ppp.domain.notification.constant.MessageCode;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.repository.PetQuerydslRepository;
import com.ppp.domain.pet.repository.PetRepository;
import com.ppp.domain.subscription.Subscription;
import com.ppp.domain.subscription.constant.Status;
import com.ppp.domain.subscription.repository.SubscriptionRepository;
import com.ppp.domain.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ppp.api.subscription.exception.ErrorCode.FORBIDDEN_PET_SPACE;
import static com.ppp.api.subscription.exception.ErrorCode.SUBSCRIBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PetRepository petRepository;
    private final PetQuerydslRepository petQuerydslRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CacheManageService cacheManageService;

    @Transactional
    public void subscribeOrUnsubscribe(Long petId, User user) {
        Pet pet = petRepository.findByIdAndIsDeletedFalse(petId)
                .orElseThrow(() -> new PetException(ErrorCode.PET_NOT_FOUND));

        subscriptionRepository.findBySubscriberAndPet(user, pet)
                .ifPresentOrElse(
                        subscriptionRepository::delete,
                        () -> {
                            subscriptionRepository.save(Subscription.builder()
                                    .subscriber(user)
                                    .status(Status.ACTIVE)
                                    .pet(pet)
                                    .build());
                            applicationEventPublisher.publishEvent(new SubscribedNotificationEvent(MessageCode.SUBSCRIBE, user, pet.getUser().getId(), pet));
                        });
        deleteCachedSubscriptionInfo(user.getId());
    }

    public List<SubscribedPetResponse> displayMySubscribedPets(User user) {
        return petQuerydslRepository.findSubscribedPetsByUserId(user.getId())
                .stream().map(SubscribedPetResponse::from)
                .collect(Collectors.toList());
    }

    public List<SubscriberResponse> displayMyPetsSubscribers(Long petId, User user) {
        validateManagePetsSubscribers(petId, user.getId());

        return subscriptionRepository.findByPetId(petId)
                .stream().map(SubscriberResponse::from)
                .collect(Collectors.toList());
    }

    private void validateManagePetsSubscribers(Long petId, String userId) {
        if (!petRepository.existsByIdAndUserIdAndIsDeletedFalse(petId, userId))
            throw new SubscriptionException(FORBIDDEN_PET_SPACE);
    }

    @Cacheable(value = "subscriptionInfo", key = "#a0")
    public SubscriptionInfoDto getUsersSubscriptionInfo(String userId) {
        Map<Boolean, List<Subscription>> subscriptionMap = subscriptionRepository.findBySubscriberId(userId)
                .stream().collect(Collectors.partitioningBy(Subscription::isBlocked));
        return SubscriptionInfoDto.of(subscriptionMap.get(false), subscriptionMap.get(true));
    }

    @Transactional
    public void blockOrUnblockSubscriber(Long petId, String subscriberId, User user) {
        validateManagePetsSubscribers(petId, user.getId());

        Subscription subscription = subscriptionRepository.findBySubscriberIdAndPetId(subscriberId, petId)
                .orElseThrow(() -> new SubscriptionException(SUBSCRIBER_NOT_FOUND));
        subscription.switchBlockStatus();
        deleteCachedSubscriptionInfo(subscriberId);
    }

    private void deleteCachedSubscriptionInfo(String userId) {
        cacheManageService.deleteCachedSubscriptionInfo(userId);
    }

    public void deleteSubscriptionsOfPet(Long petId) {
        List<Subscription> subscriptions = subscriptionRepository.findByPetId(petId);
        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(s -> cacheManageService.deleteCachedSubscriptionInfo(s.getSubscriber().getId()));
            subscriptionRepository.deleteAllInBatch(subscriptions);
        }
    }
}
