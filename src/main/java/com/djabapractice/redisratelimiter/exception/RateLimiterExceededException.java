package com.djabapractice.redisratelimiter.exception;

/**
 * Exception thrown when the rate limiter exceeds the maximum allowed requests.
 */
public class RateLimiterExceededException extends RuntimeException {
    public RateLimiterExceededException() {
    }
}
