package com.mdev.chatcord.server.authentication.dto;

import com.mdev.chatcord.server.user.dto.ProfileDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private ProfileDetails profileDetails;
}
