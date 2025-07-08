package com.mdev.chatcord.server.message.dto;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.EMessageType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessageDTO implements Serializable {

    private UUID messageUUID;
    private UUID chatUUID;
    private ChatType chatType;
    private String content;
    private EMessageType type;
    private MessageDTO replyTo;
    private ChatMemberDTO sender; // uuid
    private ChatMemberDTO receiver; // Can be username#tag or guildId << this is currently is the group.
    private LocalDateTime sentAt;
    private LocalDateTime seenAt;
    private boolean isEdited;
    private boolean isPinned;
    private EMessageStatus messageStatus;

}
