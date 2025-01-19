package com.djabapractice.redisratelimiter.service;

/**
 * Interface defining the contract for a rate limiter.
 */
public interface RateLimiter {

    /**
     * Checks if the user is allowed to make a request within the rate limit.
     *
     * @param userId The user ID.
     */
    void isAllowed(String userId);
}
