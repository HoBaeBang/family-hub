package com.familyhub.redis.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String PREFIX = "refresh_token:";
    private static final Duration TTL   = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public void save(String tokenId, String memberId) {
        redisTemplate.opsForValue().set(PREFIX + tokenId, memberId, TTL);
    }

    public Optional<String> findMemberIdByTokenId(String tokenId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + tokenId));
    }

    public void delete(String tokenId) {
        redisTemplate.delete(PREFIX + tokenId);
    }
}
