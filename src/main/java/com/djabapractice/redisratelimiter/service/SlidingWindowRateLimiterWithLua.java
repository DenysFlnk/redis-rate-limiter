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
            // TODO: Write Lua script for Sliding Window Rate Limiter
            //   Lua script explanation:
            //      1. `local varName`: Defines a local variable.
            //      2. `KEYS[index]`: Accesses a key parameter by its index (1-based).
            //      3. `ARGV[index]`: Accesses an argument parameter by its index (1-based).
            //      4. `tonumber(value)`: Converts a value to a number.
            //      5. `redis.call(command, ...)`: Executes a Redis command, e.g., `ZADD`, `ZREMRANGEBYSCORE`.
            //      6. `ZREMRANGEBYSCORE key min max`: Removes elements in the sorted set within a score range.
            //      7. `ZCARD key`: Returns the number of elements in the sorted set.
            //      8. `if condition then ... elseif condition then ... else ... end`: Defines conditional logic.
            //      9. Return values (e.g., 0 for limit exceeded, 1 for allowed).
            "-- TODO: Replace this comment with the Lua script.";

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

    /**
     * Checks if the user is allowed within the current sliding window.
     *
     * <p>
     * TODO: Implement logic to interact with Redis using the Lua script to check rate limits
     *  Steps:
     *   1. Obtain a Jedis resource from the pool.
     *   2. Generate a Redis key using the RedisKeySchema utility.
     *   3. Evaluate the Lua script using the generated key and arguments. (eval method)
     *   4. Handle the result of the script execution (allow or throw exception).
     *   5. Ensure the Jedis resource is properly closed.
     **/
    @Override
    public void isAllowed(String userId) {
        throw new RuntimeException("Not Implemented");
    }
}
