package com.djabapractice.redisratelimiter.service;

import com.djabapractice.redisratelimiter.exception.RateLimiterExceededException;
import com.djabapractice.redisratelimiter.util.RedisKeySchema;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

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
     * @param userId the user ID to check
     */
    @Override
    public void isAllowed(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisKeySchema.getSlidingRateLimiterKey("limiter", userId, windowSizeMs, maxHits);

            Transaction transaction = jedis.multi();
            long currentMillis = Instant.now().toEpochMilli();
            transaction.zadd(key, currentMillis,currentMillis + "-" + Math.random());
            transaction.zremrangeByScore(key, 0, currentMillis - (double) windowSizeMs);
            Response<Long> count = transaction.zcard(key);
            transaction.exec();

            if (count.get() > maxHits) {
                throw new RateLimiterExceededException();
            }
        }
    }
}
