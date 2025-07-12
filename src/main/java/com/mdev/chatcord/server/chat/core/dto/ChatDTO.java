package com.mdev.chatcord.server.chat.core.dto;

import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatDTO {
    private UUID uuid;
    private String chatType;
    private LocalDateTime createdAt;
    private List<ChatMemberDTO> chatMembersDto;
    private List<MessageDTO> messages;
    private ChatNotification chatNotification;
}
