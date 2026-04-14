package com.familyhub.redis.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    RefreshTokenRepository repository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        repository = new RefreshTokenRepository(redisTemplate);
    }

    @Test
    void save_stores_memberId_with_correct_key_and_ttl() {
        repository.save("uuid-1", "42");
        verify(valueOps).set("refresh_token:uuid-1", "42", Duration.ofDays(7));
    }

    @Test
    void findMemberIdByTokenId_returns_stored_value() {
        when(valueOps.get("refresh_token:uuid-1")).thenReturn("42");
        assertThat(repository.findMemberIdByTokenId("uuid-1")).isEqualTo(Optional.of("42"));
    }

    @Test
    void findMemberIdByTokenId_returns_empty_when_not_found() {
        when(valueOps.get("refresh_token:missing")).thenReturn(null);
        assertThat(repository.findMemberIdByTokenId("missing")).isEmpty();
    }

    @Test
    void delete_removes_correct_key() {
        repository.delete("uuid-1");
        verify(redisTemplate).delete("refresh_token:uuid-1");
    }
}
