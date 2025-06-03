package com.mdev.chatcord.server.friend.dto;

import com.mdev.chatcord.server.friend.service.EFriendStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ContactPreview {

    UUID uuid; // shared between private and group
    String displayName; // friend's name or group name
    String avatarUrl;
    String avatarColor;
    String lastMessage;
    LocalDateTime lastMessageAt;
    String lastMessageSender; // name or null if not applicable
    boolean isGroup;
    EFriendStatus friendStatus; // only if it's a friend, else null

}
