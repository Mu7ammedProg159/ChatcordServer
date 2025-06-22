package com.mdev.chatcord.server.user.dto;

import com.mdev.chatcord.server.user.service.EUserState;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class UserStatusDetails {
    private String userUuid;
    private EUserState state;
}
