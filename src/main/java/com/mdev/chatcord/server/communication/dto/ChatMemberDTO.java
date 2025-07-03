package com.mdev.chatcord.server.communication.dto;

import com.mdev.chatcord.server.communication.model.ChatRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMemberDTO {
    private String uuid;
    private String username;
    private String tag;
    private String avatarUrl;
    private String avatarColor;
    private String role; // ADMIN, MOD, MEMBER, etc.
}
