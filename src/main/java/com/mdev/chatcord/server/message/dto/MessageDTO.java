package com.mdev.chatcord.server.message.dto;

import com.mdev.chatcord.server.chat.ChatType;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessageDTO implements Serializable {

    private ChatType chatType;
    private String content;
    private UUID sender; // uuid
    private UUID receiver; // Can be username#tag or guildId << this is currently is the group.
    private long timestamp;
    private boolean isEdited;
    private EMessageStatus messageStatus;


}
