package com.mdev.chatcord.server.friend.model;

import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.user.model.Account;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account owner;

    @ManyToOne
    private Account friend;

    @Enumerated(EnumType.STRING)
    private EFriendStatus friendStatus;

    private LocalDateTime addedAt;

    public Friend(Account owner, Account friend, EFriendStatus friendStatus, LocalDateTime addedAt) {
        this.owner = owner;
        this.friend = friend;
        this.friendStatus = friendStatus;
        this.addedAt = addedAt;
    }
}
