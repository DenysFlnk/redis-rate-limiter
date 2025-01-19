package com.djabapractice.redisratelimiter.service;

import com.djabapractice.redisratelimiter.exception.RateLimiterExceededException;
import com.djabapractice.redisratelimiter.util.RedisKeySchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * A Sliding Window Rate Limiter that allows rate limiting with granularity using timestamps.
 */
@Component
public class SlidingWindowRateLimiter implements RateLimiter {
    private final JedisPool jedisPool;
    private final int maxHits;
    private final int windowSizeMs;

    /**
     * Constructor to initialize the Sliding Window Rate Limiter.
     *
     * @param jedisPool    Jedis connection pool.
     * @param maxHits      Maximum number of requests allowed within the window size.
     * @param windowSizeMs Window size in milliseconds.
     */
    public SlidingWindowRateLimiter(
            JedisPool jedisPool,
            @Value("${app.rate-limiter.max-hits}") int maxHits,
            @Value("${app.rate-limiter.window-size-ms}") int windowSizeMs
    ) {
        this.jedisPool = jedisPool;
        this.maxHits = maxHits;
        this.windowSizeMs = windowSizeMs;
    }

    /**
     * Checks if the user is allowed within the current sliding window.
     * <p>
     * TODO Steps:
     *   1. Get a Redis connection from JedisPool.
     *   2. Generate a unique key using userId, window size, and maxHits.
     *   3. Add a new entry to a sorted set with the current timestamp.
     *   4. Remove entries older than the sliding window size.
     *   5. Count the number of entries in the sorted set.
     *   6. If the count exceeds maxHits, throw RateLimiterExceededException.
     *   7. Handle exceptions and ensure resources are closed properly.
     *
     * @param userId the user ID to check
     */
    @Override
    public void isAllowed(String userId) {
        throw new RuntimeException("Not Implemented!");
    }
}
