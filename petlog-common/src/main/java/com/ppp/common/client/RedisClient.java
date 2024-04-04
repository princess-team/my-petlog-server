package com.ppp.common.client;

import com.ppp.domain.common.constant.Domain;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisClient {
    private final RedisTemplate<String, String> redisTemplate;

    public void addValue(Domain domain, String key, String value) {
        redisTemplate.opsForValue().setIfAbsent(domain.name() + key, value);
    }

    public void deleteValue(Domain domain, String key) {
        redisTemplate.opsForValue().getAndDelete(domain.name() + key);
    }

    public void incrementValue(Domain domain, String key) {
        redisTemplate.opsForValue().increment(domain.name() + key);
    }

    public void decrementValue(Domain domain, String key) {
        redisTemplate.opsForValue().decrement(domain.name() + key);
    }

    public Optional<String> getValue(Domain domain, String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(domain.name() + key));
    }

    public void addValue(Domain domain, Long key, String value) {
        redisTemplate.opsForValue().setIfAbsent(domain.name() + key, value);
    }

    public void deleteValue(Domain domain, Long key) {
        redisTemplate.opsForValue().getAndDelete(domain.name() + key);
    }

    public Long incrementValue(Domain domain, Long key) {
        return redisTemplate.opsForValue().increment(domain.name() + key);
    }

    public Long decrementValue(Domain domain, Long key) {
        return redisTemplate.opsForValue().decrement(domain.name() + key);
    }

    public Optional<String> getValue(Domain domain, Long key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(domain.name() + key));
    }

    public void addValueToSet(Domain domain, String key, String value) {
        redisTemplate.opsForSet().add(domain.name() + key, value);
    }

    public void removeValueToSet(Domain domain, String key, String value) {
        redisTemplate.opsForSet().remove(domain.name() + key, value);
    }


    public boolean isValueExistInSet(Domain domain, String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(domain.name() + key, value));
    }

    public Long getSizeOfSet(Domain domain, String key) {
        return redisTemplate.opsForSet().size(domain.name() + key);
    }


    public void removeKeyToSet(Domain domain, String key) {
        redisTemplate.opsForSet().pop(domain.name() + key);
    }

    public void addValueToSet(Domain domain, Long key, String value) {
        redisTemplate.opsForSet().add(domain.name() + key, value);
    }


    public void removeValueToSet(Domain domain, Long key, String value) {
        redisTemplate.opsForSet().remove(domain.name() + key, value);
    }

    public Set<String> getSet(Domain domain, Long key) {
        return redisTemplate.opsForSet().members(domain.name() + key);
    }

    public boolean isValueExistInSet(Domain domain, Long key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(domain.name() + key, value));
    }

    public Long getSizeOfSet(Domain domain, Long key) {
        return redisTemplate.opsForSet().size(domain.name() + key);
    }

    public void removeKeyToSet(Domain domain, Long key) {
        redisTemplate.opsForSet().pop(domain.name() + key);
    }

    public void setValues(String key, String data, Duration duration) {
        redisTemplate.opsForValue().set(key, data, duration);
    }

    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
