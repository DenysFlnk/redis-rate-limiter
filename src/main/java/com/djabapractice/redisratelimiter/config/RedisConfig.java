package com.djabapractice.redisratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPool jedisPool() {
        return new JedisPool();
    }
}
