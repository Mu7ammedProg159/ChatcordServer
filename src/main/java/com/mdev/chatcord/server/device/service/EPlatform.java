package com.mdev.chatcord.server.device.service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EPlatform {
    DESKTOP("Chatcord-Desktop-App"),
    WEB("User-Agent"),
    MOBILE("Chatcord-Mobile-App");

    private final String userAgent;
}
