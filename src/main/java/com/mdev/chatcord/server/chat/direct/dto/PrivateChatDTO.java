package com.mdev.chatcord.server.chat.direct.dto;

import com.mdev.chatcord.server.chat.core.dto.ChatDTO;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrivateChatDTO {
    ContactPreview contactPreview;
    ChatDTO chatDTO;
}
