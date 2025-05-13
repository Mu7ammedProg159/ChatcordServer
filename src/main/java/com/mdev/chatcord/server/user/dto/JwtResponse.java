package com.mdev.chatcord.server.user.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtResponse {
    private String token;
    public String getToken() { return token; }
}
