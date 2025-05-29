package com.mdev.chatcord.server.chat.core.dto;

import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatDTO {
    private String chatType;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private String lastMessageSender;
    private List<ChatMemberDTO> chatMembersDto;
    private List<MessageDTO> messages;
    private UnreadStatus unreadStatus;
}
