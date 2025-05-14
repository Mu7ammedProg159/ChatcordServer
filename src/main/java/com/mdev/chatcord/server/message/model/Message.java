package com.mdev.chatcord.server.message.model;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.user.model.User;
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
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    private User sender;

    private String message;

    private LocalDateTime sentAt;
    private LocalDateTime seenAt;

    @Enumerated(EnumType.STRING)
    private EMessageStatus messageState;

    @ManyToOne
    private Chat chat;
}
