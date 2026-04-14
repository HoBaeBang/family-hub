package com.familyhub.api;

import com.familyhub.api.config.TestJwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtConfig.class)
class AppApiApplicationTest {

    @MockitoBean
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }
}
