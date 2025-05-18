package com.mdev.chatcord.server.authentication.dto;

import com.mdev.chatcord.server.device.dto.DeviceDto;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JwtRequest {

    @Email(message = "Email address does not exists.")
    private String email;
    private String password;
    private String username;
    private DeviceDto deviceDto;

    public JwtRequest(@Email(message = "Email address does not exists.") String email, String password) {
        this.email = email;
        this.password = password;
    }
}
