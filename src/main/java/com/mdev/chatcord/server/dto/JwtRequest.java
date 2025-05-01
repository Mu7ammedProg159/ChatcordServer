package com.mdev.chatcord.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRequest {
    private String email;
    private String password;
    private String username;

    public JwtRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
