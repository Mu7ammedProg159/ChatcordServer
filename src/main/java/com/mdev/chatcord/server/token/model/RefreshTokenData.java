package com.mdev.chatcord.server.token.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenData {
    private String jwt;
    private String email;
    private String deviceId;
    private Instant expiry;
}
