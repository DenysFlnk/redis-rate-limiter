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
 * A Fixed Window Rate Limiter that limits the number of requests a user can make in a specified interval.
 */
@Component
public class FixedWindowRateLimiter implements RateLimiter {
    private final JedisPool jedisPool;
    private final int maxHits;
    private final int intervalInMinutes;
    private final int expirationInSeconds;

    /**
     * Constructor to initialize the Fixed Window Rate Limiter.
     *
     * @param jedisPool         Jedis connection pool.
     * @param maxHits           Maximum number of requests allowed per interval.
     * @param intervalInMinutes Interval duration in minutes.
     */
    public FixedWindowRateLimiter(
            JedisPool jedisPool,
            @Value("${app.rate-limiter.max-hits}") int maxHits,
            @Value("${app.rate-limiter.interval-in-minutes}") int intervalInMinutes
    ) {
        this.jedisPool = jedisPool;
        this.maxHits = maxHits;
        this.intervalInMinutes = intervalInMinutes;
        this.expirationInSeconds = intervalInMinutes * 60;
    }

    /**
     * Checks if the user is allowed within the current fixed window.
     * <p>
     * TODO Steps:
     *   1. Get a Redis connection from JedisPool.
     *   2. Generate a unique key using userId, minute block, and maxHits.
     *   3. Increment the hit count in Redis for the key.
     *   4. Set expiration time for the key.
     *   5. Check if the hit count exceeds the maxHits.
     *   6. If exceeded, throw RateLimiterExceededException.
     *   7. Handle exceptions and ensure resources are closed properly.
     *
     * @param userId the user ID to check
     */
    @Override
    public void isAllowed(String userId) {
        throw new RuntimeException("Not Implemented!");
    }

    /**
     * Calculates the minute block for the given time.
     * <p>
     * TODO Steps:
     * 1. Calculate the current minute of the day.
     * 2. Divide the minute by intervalInMinutes to get the block.
     *
     * @param time the time to calculate the block
     * @return the minute block
     */
    private int getMinuteBlock(ZonedDateTime time) {
        throw new RuntimeException("Not Implemented!");
    }
}
