package com.aiplms.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {

    private final StringRedisTemplate redisTemplate;

    public long tryConsume(String key, long limit, Duration window) {
        String k = "rl:" + key;

        Long current = redisTemplate.execute((connection) -> {
            byte[] rawKey = k.getBytes();
            Long val = connection.incr(rawKey);
            if (val != null && val == 1L) {
                connection.pExpire(rawKey, window.toMillis());
            }
            return val;
        }, true);

        if (current == null) return -1;
        if (current <= limit) {
            return limit - current;
        }
        return -1;
    }
}
