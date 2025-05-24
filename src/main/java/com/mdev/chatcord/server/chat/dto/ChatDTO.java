package com.mdev.chatcord.server.chat.dto;

import com.mdev.chatcord.server.chat.ChatType;
import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.friend.dto.FriendDTO;
import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
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
    private ChatType chatType;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private String lastMessageSender;
    private List<ChatMemberDTO> chatMembersDto;
    private List<MessageDTO> messages;
    private UnreadStatus unreadStatus;
    private EFriendStatus relationship;

}
