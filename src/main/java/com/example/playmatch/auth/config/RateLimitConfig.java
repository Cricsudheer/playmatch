package com.example.playmatch.auth.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    // Caffeine cache with TTL to prevent memory leak
    // - Entries expire 1 hour after last access
    // - Maximum 10,000 entries to prevent unbounded growth
    // - Evicts entries automatically when limits are reached
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(10_000)
            .build();

    @Bean
    public Cache<String, Bucket> buckets() {
        return buckets;
    }

    public Bucket resolveBucket(String key) {
        return buckets.get(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15))))
            .build();
    }
}
