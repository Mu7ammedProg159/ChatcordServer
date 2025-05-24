package com.mdev.chatcord.server.communication.dto;

import com.mdev.chatcord.server.communication.service.PrivilegeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ChatRoleDTO {
    private String name;
}
