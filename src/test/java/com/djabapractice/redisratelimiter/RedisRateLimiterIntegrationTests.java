package com.djabapractice.redisratelimiter;

import com.djabapractice.redisratelimiter.exception.RateLimiterExceededException;
import com.djabapractice.redisratelimiter.service.FixedWindowRateLimiter;
import com.djabapractice.redisratelimiter.service.SlidingWindowRateLimiter;
import com.djabapractice.redisratelimiter.service.SlidingWindowRateLimiterWithLua;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class RedisRateLimiterIntegrationTests {

    @Container
    private static final ComposeContainer redisContainer =
            new ComposeContainer(new File("docker-compose.yml"))
                    .withExposedService("redis", 6379);

    private JedisPool jedisPool;

    @BeforeAll
    static void setup() {
        redisContainer.start();
    }

    @AfterAll
    static void tearDown() {
        redisContainer.stop();
    }

    @BeforeEach
    void init() {
        jedisPool = new JedisPool(redisContainer.getServiceHost("redis", 6379),
                redisContainer.getServicePort("redis", 6379));
    }

    @Test
    @DisplayName("Fixed Window Rate Limiter: Should allow 5 requests in 1 minute window")
    void testFixedWindowRateLimiter_withinLimit_shouldAllow() {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(jedisPool, 5, 1);
        String userId = UUID.randomUUID().toString();
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
        }
    }

    @Test
    @DisplayName("Fixed Window Rate Limiter: Should throw exception after exceeding limit")
    void testFixedWindowRateLimiter_exceedLimit_shouldThrowException() {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(jedisPool, 5, 1);

        String userId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
        }

        assertThrows(RateLimiterExceededException.class, () -> rateLimiter.isAllowed(userId));
    }

    @Test
    @DisplayName("Sliding Window Rate Limiter: Should allow 5 requests within window")
    void testSlidingWindowRateLimiter_withinLimit_shouldAllow() {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(jedisPool, 5, 60000);

        String userId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
        }
    }

    @Test
    @DisplayName("Sliding Window Rate Limiter: Should throw exception after exceeding limit")
    void testSlidingWindowRateLimiter_exceedLimit_shouldThrowException() {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(jedisPool, 5, 60000);

        String userId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
        }

        assertThrows(RateLimiterExceededException.class, () -> rateLimiter.isAllowed(userId));
    }

    @Test
    @DisplayName("Sliding Window Rate Limiter with Lua: Should allow 5 requests within window")
    void testSlidingWindowRateLimiterWithLua_withinLimit_shouldAllow() {
        SlidingWindowRateLimiterWithLua rateLimiter = new SlidingWindowRateLimiterWithLua(jedisPool, 5, 60000);

        String userId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
        }
    }

    @Test
    @DisplayName("Sliding Window Rate Limiter with Lua: Should throw exception after exceeding limit")
    void testSlidingWindowRateLimiterWithLua_exceedLimit_shouldThrowException() {
        SlidingWindowRateLimiterWithLua rateLimiter = new SlidingWindowRateLimiterWithLua(jedisPool, 5, 60000);

        String userId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
        }

        assertThrows(RateLimiterExceededException.class, () -> rateLimiter.isAllowed(userId));
    }

    @Nested
    @DisplayName("[Optional tests]:")
    class MultithreadedTests {
        @Test
        @DisplayName("Sliding Window Rate Limiter [1 second sleep after invocation]: Should allow 10 requests within 5 seconds window ")
        void testSlidingWindowRateLimiter_withinLimit_shouldAllow() throws InterruptedException {
            SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(jedisPool, 5, 5000);

            String userId = UUID.randomUUID().toString();

            for (int i = 0; i < 10; i++) {
                assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
                Thread.sleep(1000);
            }
        }

        @Test
        @DisplayName("Sliding Window Rate Limiter [1 second sleep after invocation]: Should allow 10 requests within 1 second window ")
        void testSlidingWindowRateLimiter_withinLimit_shouldAllow_1_request_in_1_second() throws InterruptedException {
            SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(jedisPool, 1, 1000);

            String userId = UUID.randomUUID().toString();

            for (int i = 0; i < 10; i++) {
                assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
                Thread.sleep(1000);
            }
        }

        @Test
        @DisplayName("Fixed Window Rate Limiter [10 second sleep after invocation]: Should allow 6 requests in 1 minute window")
        void testFixedWindowRateLimiter_withinLimit_shouldAllow() throws InterruptedException {
            FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(jedisPool, 5, 1);
            String userId = UUID.randomUUID().toString();
            for (int i = 0; i < 6; i++) {
                assertDoesNotThrow(() -> rateLimiter.isAllowed(userId));
                Thread.sleep(10000);
            }
        }
    }
}
