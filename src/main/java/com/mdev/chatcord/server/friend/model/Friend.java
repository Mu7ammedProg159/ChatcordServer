package com.mdev.chatcord.server.friend.model;

import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Entity
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private User friend;

    @Enumerated(EnumType.STRING)
    private EFriendStatus friendStatus;

    private LocalDateTime addedAt;

}
