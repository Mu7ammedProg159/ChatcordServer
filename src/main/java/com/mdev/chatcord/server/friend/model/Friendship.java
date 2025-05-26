package com.mdev.chatcord.server.friend.model;

import com.mdev.chatcord.server.BaseEntity;
import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.user.model.Profile;
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
public class Friendship extends BaseEntity {

    @ManyToOne
    private Profile owner;

    @ManyToOne
    private Profile friend;

    @Enumerated(EnumType.STRING)
    private EFriendStatus friendStatus;

    private LocalDateTime addedAt;

}
