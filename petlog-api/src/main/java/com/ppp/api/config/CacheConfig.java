package com.ppp.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.ppp.domain.common.constant.CacheValue.*;

@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(defaultRedisCacheConfiguration()
                        .entryTtl(Duration.ofMinutes(60)))
                .withInitialCacheConfigurations(redisCacheConfigurationMap())
                .build();
    }

    private RedisCacheConfiguration defaultRedisCacheConfiguration() {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    private Map<String, RedisCacheConfiguration> redisCacheConfigurationMap() {
        Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();
        cacheConfigurationMap.put(PET_SPACE_AUTHORITY.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(30)));
        cacheConfigurationMap.put(DIARY_COMMENT_COUNT.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(60)));
        cacheConfigurationMap.put(DIARY_COMMENT_LIKE_COUNT.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(30)));
        cacheConfigurationMap.put(DIARY_MOST_USED_TERMS.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(40)));
        cacheConfigurationMap.put(DIARY_COMMENT_RE_COMMENT_COUNT.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(60)));
        cacheConfigurationMap.put(DIARY_ACCESS_AUTHORITY.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofSeconds(70)));
        cacheConfigurationMap.put(SUBSCRIPTION_INFO.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(5)));
        cacheConfigurationMap.put(TOTAL_PUBLIC_DIARY_COUNT.getValue(),
                defaultRedisCacheConfiguration().entryTtl(Duration.ofMinutes(30)));
        return cacheConfigurationMap;
    }

}
