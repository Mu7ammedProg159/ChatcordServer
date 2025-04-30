package com.mdev.chatcord.server.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse {
    private String token;
    public String getToken() { return token; }
}
