package com.ppp.api.diary.service;

import com.ppp.common.client.RedisClient;
import com.ppp.domain.common.constant.Domain;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DiaryRedisService {
    private final RedisClient redisClient;

    public boolean isLikeExistByDiaryIdAndUserId(Long diaryId, String userId) {
        return redisClient.isValueExistInSet(Domain.DIARY_LIKE, diaryId, userId);
    }

    @Cacheable(value = "diaryLikeCount")
    public Integer getLikeCountByDiaryId(Long diaryId) {
        Long likeCount = redisClient.getSizeOfSet(Domain.DIARY_LIKE, diaryId);
        assert likeCount != null;

        return likeCount.intValue();
    }

    @CachePut(value = "diaryLikeCount", key = "#a0")
    public Integer registerLikeByDiaryIdAndUserId(Long diaryId, String userId) {
        redisClient.addValueToSet(Domain.DIARY_LIKE, diaryId, userId);
        Long likeCount = redisClient.getSizeOfSet(Domain.DIARY_LIKE, diaryId);
        assert likeCount != null;

        return likeCount.intValue();
    }

    @CachePut(value = "diaryLikeCount", key = "#a0")
    public Integer cancelLikeByDiaryIdAndUserId(Long diaryId, String userId) {
        redisClient.removeValueToSet(Domain.DIARY_LIKE, diaryId, userId);
        Long likeCount = redisClient.getSizeOfSet(Domain.DIARY_LIKE, diaryId);
        assert likeCount != null;

        return likeCount.intValue();
    }

    @CacheEvict(value = "diaryLikeCount")
    public void deleteAllLikeByDiaryId(Long diaryId) {
        redisClient.removeKeyToSet(Domain.DIARY_LIKE, diaryId);
    }

    public Set<String> getLikedUserIdsByDiaryId(Long diaryId){
        return redisClient.getSet(Domain.DIARY_LIKE, diaryId);
    }
}
