package com.mdev.chatcord.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
    private String username;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
