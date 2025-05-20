package com.mdev.chatcord.server.redis.service;

public interface RefreshTokenStore {
    void save(String username, String deviceId, String token, long ttlSeconds);
    boolean exists(String username, String deviceId, String token);
    void remove(String username, String deviceId);
}