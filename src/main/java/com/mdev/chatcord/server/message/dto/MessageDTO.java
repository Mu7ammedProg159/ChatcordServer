package com.mdev.chatcord.server.message.dto;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessageDTO implements Serializable {

    private ChatType chatType;
    private String content;
    private ChatMemberDTO sender; // uuid
    private ChatMemberDTO receiver; // Can be username#tag or guildId << this is currently is the group.
    private LocalDateTime timestamp;
    private boolean isEdited;
    private EMessageStatus messageStatus;


}
