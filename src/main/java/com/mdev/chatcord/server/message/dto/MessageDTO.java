package com.mdev.chatcord.server.message.dto;

import com.mdev.chatcord.server.chat.ChatType;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.user.dto.Profile;
import com.mdev.chatcord.server.user.dto.ProfileDTO;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessageDTO implements Serializable {

    private ChatType chatType;
    private String content;
    private String sender; // uuid
    private String receiver; // Can be username#tag or guildId << this is currently is the group.
    private long timestamp;
    private boolean isEdited;
    private EMessageStatus messageStatus;


}
