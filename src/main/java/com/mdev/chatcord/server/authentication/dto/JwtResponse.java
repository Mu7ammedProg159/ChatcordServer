package com.mdev.chatcord.server.authentication.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtResponse {
    private String token;
    public String getToken() { return token; }
}
