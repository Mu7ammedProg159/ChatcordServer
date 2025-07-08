package com.mdev.chatcord.server.message.model;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.EMessageType;
import com.mdev.chatcord.server.user.model.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne
    private Profile sender; // Who sent ?

    @ManyToOne
    private Chat chat; // Where sent ?

    @Enumerated(EnumType.STRING)
    private ChatType context; // What the context or place the message is going to be sent at.

    private String message; // What you said ?

    @Enumerated(EnumType.STRING)
    private EMessageType type; // Is the message a text or image or .. etc

    @ManyToOne
    private Message replyTo; // Is the message a reply to another message ? if so, then what the message is ?

    private boolean isEdited = false; // is the message edited ?
    private boolean isPinned = false; // is the message pinned ?

    private LocalDateTime sentAt; // When Sent ?
    private LocalDateTime seenAt; // When read ?

    @Enumerated(EnumType.STRING)
    private EMessageStatus state; // Reached or not ?

    public Message(UUID uuid, Profile sender, Chat chat, ChatType context, String message, EMessageType type,
                   Message replyTo, boolean isEdited, boolean isPinned, LocalDateTime sentAt, LocalDateTime seenAt,
                   EMessageStatus state) {
        this.uuid = uuid;
        this.sender = sender;
        this.chat = chat;
        this.context = context;
        this.message = message;
        this.type = type;
        this.replyTo = replyTo;
        this.isEdited = isEdited;
        this.isPinned = isPinned;
        this.sentAt = sentAt;
        this.seenAt = seenAt;
        this.state = state;
    }
}
