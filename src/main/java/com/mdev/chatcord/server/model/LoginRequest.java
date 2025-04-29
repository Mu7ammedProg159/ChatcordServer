package com.mdev.chatcord.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
