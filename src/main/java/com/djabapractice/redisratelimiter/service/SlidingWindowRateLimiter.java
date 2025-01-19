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
     * Checks if the user is allowed to make a request within the sliding window rate limit.
     *
     * @param userId The user ID.
     * @throws RateLimiterExceededException if the user exceeds the allowed number of requests.
     */
    @Override
    public void isAllowed(String userId) {
        try (Jedis jedis = jedisPool.getResource();
             Transaction transaction = jedis.multi()) {

            String key = RedisKeySchema.getSlidingRateLimiterKey("limiter", userId, windowSizeMs, maxHits);

            long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
            transaction.zadd(key, timestamp, timestamp + "-" + userId);
            transaction.zremrangeByScore(key, 0, timestamp - windowSizeMs);
            Response<Long> numberOfHits = transaction.zcard(key);
            transaction.exec();

            if (numberOfHits.get() > maxHits) {
                throw new RateLimiterExceededException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
