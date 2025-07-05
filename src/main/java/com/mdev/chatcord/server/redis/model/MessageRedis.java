package com.mdev.chatcord.server.redis.model;

import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.model.Profile;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageRedis {
    private Long id;
    private String content;
    private Profile sender;
    private LocalDateTime sentAt; // When Sent ?
    private LocalDateTime seenAt; // When read ?
    private boolean isEdited;
    private EMessageStatus messageState; // Reached or not ?
}
