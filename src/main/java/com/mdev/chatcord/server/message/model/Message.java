package com.mdev.chatcord.server.message.model;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.common.BaseEntity;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.user.model.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Message extends BaseEntity {

    @ManyToOne
    private Profile sender; // Who sent ?

    @ManyToOne
    private Chat chat; // Where sent ?

    private String message; // What you said ?

    private LocalDateTime sentAt; // When Sent ?
    private LocalDateTime seenAt; // When read ?

    @Enumerated(EnumType.STRING)
    private EMessageStatus messageState; // Reached or not ?

}
