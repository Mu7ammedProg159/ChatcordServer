package com.mdev.chatcord.server.redis.model;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.EMessageType;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.model.Profile;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageRedis {
    private Long id;
    private Long chatId;
    private UUID uuid;
    private UUID chatUUID;
    private ChatType context;
    private String content;
    private EMessageType type;
    private Long replyToId;
    private Profile sender;
    private LocalDateTime sentAt; // When Sent ?
    private LocalDateTime seenAt; // When read ?
    private boolean isEdited;
    private boolean isPinned;
    private EMessageStatus messageState; // Reached or not ?
}
