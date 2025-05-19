package com.mdev.chatcord.server.authentication.dto;

import lombok.*;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshDto {
    private String deviceId;
    private String refreshToken;
}
