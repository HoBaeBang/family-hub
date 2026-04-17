package com.familyhub.redis.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TempCodeRepository {

    private static final String PREFIX = "auth:temp_code:";
    private static final Duration TTL   = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;

    public String save(String memberId) {
        String code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + code, memberId, TTL);
        return code;
    }

    public Optional<String> findAndDelete(String code) {
        String value = redisTemplate.opsForValue().get(PREFIX + code);
        if (value != null) {
            redisTemplate.delete(PREFIX + code);
        }
        return Optional.ofNullable(value);
    }
}
