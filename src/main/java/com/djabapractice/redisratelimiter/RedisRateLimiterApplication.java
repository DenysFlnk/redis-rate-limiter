package com.djabapractice.redisratelimiter;

import com.djabapractice.redisratelimiter.service.RateLimiter;
import com.djabapractice.redisratelimiter.service.SlidingWindowRateLimiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RedisRateLimiterApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(RedisRateLimiterApplication.class, args);
		RateLimiter rateLimiter = context.getBean(SlidingWindowRateLimiter.class);
		for (int i = 0; i < 1; i++) {
			rateLimiter.isAllowed("dhalchenko");
		}
	}

}
