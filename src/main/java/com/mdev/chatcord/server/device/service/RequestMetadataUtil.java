package com.mdev.chatcord.server.device.service;

import jakarta.servlet.http.HttpServletRequest;

public class RequestMetadataUtil {

    public static String retrieveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    public static String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
