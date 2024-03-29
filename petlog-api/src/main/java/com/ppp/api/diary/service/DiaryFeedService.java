package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.response.DiaryFeedResponse;
import com.ppp.api.subscription.dto.transfer.SubscriptionInfoDto;
import com.ppp.api.subscription.service.SubscriptionService;
import com.ppp.domain.diary.dto.PetDiaryDto;
import com.ppp.domain.diary.repository.DiaryQuerydslRepository;
import com.ppp.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryFeedService {

    private final DiaryQuerydslRepository diaryQuerydslRepository;
    private final SubscriptionService subscriptionService;
    private final DiaryCommentRedisService diaryCommentRedisService;
    private final DiaryRedisService diaryRedisService;

    public Set<DiaryFeedResponse> retrieveDiaryFeed(User user, int page, int size) {
        SubscriptionInfoDto subscriptionInfo = subscriptionService.getUsersSubscriptionInfo(user.getId());

        List<DiaryFeedResponse> subscribedPetsDiaries = new ArrayList<>();
        if (!subscriptionInfo.subscribedPetIds().isEmpty()) {
            subscribedPetsDiaries = diaryQuerydslRepository
                    .findSubscribedPetsDiariesBySubscription(subscriptionInfo.subscribedPetIds(), PageRequest.of(page, size / 2))
                    .stream().map(dto -> toDiaryFeedResponse(dto, user.getId(), subscriptionInfo.subscribedPetIds())).toList();
        }
        List<DiaryFeedResponse> randomPetDiaries = diaryQuerydslRepository.findRandomPetsDiaries(subscriptionInfo.blockedPetIds(), PageRequest.of(page, size - subscribedPetsDiaries.size()))
                .stream().map(dto -> toDiaryFeedResponse(dto, user.getId())).toList();
        Set<DiaryFeedResponse> response = new HashSet<>(subscribedPetsDiaries);
        response.addAll(randomPetDiaries);
        return response;
    }

    private DiaryFeedResponse toDiaryFeedResponse(PetDiaryDto dto, String userId, Set<Long> subscribedPetIds) {
        return DiaryFeedResponse.from(dto,
                diaryCommentRedisService.getDiaryCommentCountByDiaryId(dto.getDiaryId()),
                diaryRedisService.isLikeExistByDiaryIdAndUserId(dto.getDiaryId(), userId),
                diaryRedisService.getLikeCountByDiaryId(dto.getDiaryId()),
                subscribedPetIds.contains(dto.getPetId()));
    }

    private DiaryFeedResponse toDiaryFeedResponse(PetDiaryDto dto, String userId) {
        return DiaryFeedResponse.from(dto,
                diaryCommentRedisService.getDiaryCommentCountByDiaryId(dto.getDiaryId()),
                diaryRedisService.isLikeExistByDiaryIdAndUserId(dto.getDiaryId(), userId),
                diaryRedisService.getLikeCountByDiaryId(dto.getDiaryId()));
    }
}
