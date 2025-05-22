package com.mdev.chatcord.server.chat.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UnreadStatus {
    private int unreadCount;
    private boolean isMuted;
    private boolean isPinned;
}
