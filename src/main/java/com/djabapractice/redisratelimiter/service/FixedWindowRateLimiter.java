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
     * Checks if the user is allowed to make a request within the rate limit.
     *
     * @param userId The user ID.
     * @throws RateLimiterExceededException if the user exceeds the allowed number of requests.
     */
    @Override
    public void isAllowed(String userId) {
        try (Jedis jedis = jedisPool.getResource();
             Transaction transaction = jedis.multi()) {

            String key = RedisKeySchema.getFixedRateLimiterKey(userId, getMinuteBlock(ZonedDateTime.now()), maxHits);
            Response<Long> numberOfHits = transaction.incr(key);
            transaction.expire(key, expirationInSeconds);
            transaction.exec();

            if (numberOfHits.get() > maxHits) {
                throw new RateLimiterExceededException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the current minute block based on the time.
     *
     * @param time The current ZonedDateTime.
     * @return The minute block as an integer.
     */
    private int getMinuteBlock(ZonedDateTime time) {
        int currentMinuteOfDay = time.getHour() * 60 + time.getMinute();
        return currentMinuteOfDay / intervalInMinutes;
    }
}
