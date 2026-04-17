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
class TempCodeRepositoryTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    TempCodeRepository repository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        repository = new TempCodeRepository(redisTemplate);
    }

    @Test
    void save_stores_memberId_with_30s_ttl_and_returns_code() {
        String code = repository.save("42");

        assertThat(code).isNotBlank();
        verify(valueOps).set(
                argThat(key -> key.startsWith("auth:temp_code:")),
                eq("42"),
                eq(Duration.ofSeconds(30))
        );
    }

    @Test
    void findAndDelete_returns_memberId_and_deletes_key() {
        when(valueOps.get("auth:temp_code:test-code")).thenReturn("42");

        Optional<String> result = repository.findAndDelete("test-code");

        assertThat(result).isEqualTo(Optional.of("42"));
        verify(redisTemplate).delete("auth:temp_code:test-code");
    }

    @Test
    void findAndDelete_returns_empty_when_not_found() {
        when(valueOps.get("auth:temp_code:missing")).thenReturn(null);

        assertThat(repository.findAndDelete("missing")).isEmpty();
        verify(redisTemplate, never()).delete(anyString());
    }
}
