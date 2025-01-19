package com.djabapractice.redisratelimiter.util;

/**
 * Utility class for generating Redis keys for different rate limiting strategies.
 */
public class RedisKeySchema {

    /**
     * Generates a Redis key for Fixed Window Rate Limiting in format: limiter:[userId]:[minute-block]:[max-hits]
     *
     * @param userId The user ID.
     * @param minuteBlock The minute block.
     * @param maxHits The maximum allowed hits.
     * @return The Redis key.
     */
    //
    public static String getFixedRateLimiterKey(String userId, int minuteBlock, int maxHits) {
        return String.format("limiter:%s:%d:%d", userId, minuteBlock, maxHits);
    }

    /**
     * Generates a Redis key for Sliding Window Rate Limiting in format: [limiter]:[name]:[windowSize]:[maxHits]
     *
     * @param userId The user ID.
     * @param windowSize The window size in milliseconds.
     * @param maxHits The maximum allowed hits.
     * @return The Redis key.
     */
    public static String getSlidingRateLimiterKey(String limiter, String userId, long windowSize, int maxHits) {
        return String.format("%s:%s:%d:%d", limiter, userId, windowSize, maxHits);
    }
}
