package com.djabapractice.redisratelimiter.service;

import com.djabapractice.redisratelimiter.exception.RateLimiterExceededException;
import com.djabapractice.redisratelimiter.util.RedisKeySchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.ZonedDateTime;

/**
 * A Sliding Window Rate Limiter implemented using a Lua script for efficient processing.
 */
@Component
public class SlidingWindowRateLimiterWithLua implements RateLimiter {
    private final JedisPool jedisPool;
    private final int maxHits;
    private final int windowSizeMs;
    private static final String LUA_SCRIPT =
            "local key = KEYS[1] " +
                    "local maxHits = tonumber(ARGV[1]) " +
                    "local windowSize = tonumber(ARGV[2]) " +
                    "local currentTime = tonumber(ARGV[3]) " +
                    "redis.call('ZREMRANGEBYSCORE', key, 0, currentTime - windowSize) " +
                    "local hits = redis.call('ZCARD', key) " +
                    "if hits >= maxHits then " +
                    "  return 0 " +
                    "else " +
                    "  redis.call('ZADD', key, currentTime, currentTime) " +
                    "  return 1 " +
                    "end";

    /**
     * Constructor to initialize the Sliding Window Rate Limiter with Lua.
     *
     * @param jedisPool    Jedis connection pool.
     * @param maxHits      Maximum number of requests allowed within the window size.
     * @param windowSizeMs Window size in milliseconds.
     */
    public SlidingWindowRateLimiterWithLua(
            JedisPool jedisPool,
            @Value("${app.rate-limiter.max-hits}") int maxHits,
            @Value("${app.rate-limiter.window-size-ms}") int windowSizeMs
    ) {
        this.jedisPool = jedisPool;
        this.maxHits = maxHits;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public void isAllowed(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisKeySchema.getSlidingRateLimiterKey("lualimiter", userId, windowSizeMs, maxHits);
            Object result = jedis.eval(
                    LUA_SCRIPT,
                    1,
                    key,
                    String.valueOf(maxHits),
                    String.valueOf(windowSizeMs),
                    String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli())
            );
            if (result.equals(0L)) {
                throw new RateLimiterExceededException();
            }
        }
    }
}
