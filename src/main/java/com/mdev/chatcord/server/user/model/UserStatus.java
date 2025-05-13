package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.user.service.EUserState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@Setter
public class UserStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private EUserState status;
}
