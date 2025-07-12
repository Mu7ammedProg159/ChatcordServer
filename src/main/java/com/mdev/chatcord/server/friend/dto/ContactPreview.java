package com.mdev.chatcord.server.friend.dto;

import com.mdev.chatcord.server.friend.enums.EFriendStatus;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ContactPreview {

    private UUID uuid; // shared between private and group
    private String displayName; // friend's name or group name
    private String tag; // friend's name or group name
    private String avatarUrl;
    private String avatarColor;
    private MessageDTO lastMessage;
    private int unreadMessages = 0;
    private boolean isGroup;
    private EFriendStatus friendStatus; // only if it's a friend, else null
    private LocalDateTime addedAt;

}
