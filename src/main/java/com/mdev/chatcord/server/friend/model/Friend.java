package com.mdev.chatcord.server.friend.model;

import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

    public Friend(User owner, User friend, EFriendStatus friendStatus, LocalDateTime addedAt) {
        this.owner = owner;
        this.friend = friend;
        this.friendStatus = friendStatus;
        this.addedAt = addedAt;
    }
}
