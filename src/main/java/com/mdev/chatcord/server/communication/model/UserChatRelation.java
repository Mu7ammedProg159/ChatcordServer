package com.mdev.chatcord.server.communication.model;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserChatRelation {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chat chat;

    private int pings = 0;

    private boolean muted;

    private LocalDateTime lastSeenAt;

    // for guild chats
    private String nickname;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Role role;

}
