package com.djabapractice.redisratelimiter.service;

import com.djabapractice.redisratelimiter.exception.RateLimiterExceededException;
import com.djabapractice.redisratelimiter.util.RedisKeySchema;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

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
     * @param userId the user ID to check
     */
    @Override
    public void isAllowed(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisKeySchema.getFixedRateLimiterKey(userId, getMinuteBlock(ZonedDateTime.now()), maxHits);

            Transaction transaction = jedis.multi();
            Response<Long> count = transaction.incr(key);
            transaction.expire(key, expirationInSeconds);
            transaction.exec();

            if (count.get() > maxHits) {
                throw new RateLimiterExceededException();
            }
        }
    }

    /**
     * Calculates the minute block for the given time.
     * <p>
     * @param time the time to calculate the block
     * @return the minute block
     */
    private int getMinuteBlock(ZonedDateTime time) {
        return (time.getHour() * 60 + time.getMinute()) / intervalInMinutes;
    }
}
