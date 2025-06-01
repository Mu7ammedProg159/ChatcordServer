package com.mdev.chatcord.server.chat.core.dto;

import com.mdev.chatcord.server.chat.core.model.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FriendshipPairDetails {
    private Chat chat;
    private Long friendId;
}
