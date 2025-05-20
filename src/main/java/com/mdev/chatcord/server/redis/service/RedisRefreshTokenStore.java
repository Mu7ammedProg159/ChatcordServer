package com.mdev.chatcord.server.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    private String key(String username, String deviceId) {
        return "refresh:" + username + ":" + deviceId;
    }

    @Override
    public void save(String username, String deviceId, String token, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(username, deviceId), token, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String username, String deviceId, String token) {
        String stored = redisTemplate.opsForValue().get(key(username, deviceId));
        return token.equals(stored);
    }

    @Override
    public void remove(String username, String deviceId) {
        redisTemplate.delete(key(username, deviceId));
    }
}