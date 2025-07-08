package com.mdev.chatcord.server.chat.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatNotification {
    private boolean isMuted;
    private boolean isFavourite;
}
