package com.mdev.chatcord.server.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse {
    private String token;
    public String getToken() { return token; }
}
