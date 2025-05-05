package com.mdev.chatcord.server.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRequest {

    @Email(message = "Email address does not exists.")
    private String email;

    private String password;
    private String username;

    public JwtRequest(@Email(message = "Email address does not exists.") String email, String password) {
        this.email = email;
        this.password = password;
    }
}
