package com.ppp.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheManageService {
    @CacheEvict(value = "petSpaceAuthority", key = "{#a0, #a1}")
    public void deleteCachedPetSpaceAuthority(String userId, Long petId) {
        log.info("Class : {}, Method : {}, CacheKey : {}", this.getClass(), "deleteCachedPetSpaceAuthority", userId + "," + petId);
    }

    @CacheEvict(value = "subscriptionInfo", key = "#a0")
    public void deleteCachedSubscriptionInfo(String userId) {
        log.info("Class : {}, Method : {}, CacheKey : {}", this.getClass(), "deleteCachedPetSpaceAuthority", userId);
    }
}
