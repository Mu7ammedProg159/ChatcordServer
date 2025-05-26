package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.user.service.EUserState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account user;

    @Enumerated(EnumType.STRING)
    private EUserState status;

    public UserStatus(Account user, EUserState status) {
        this.user = user;
        this.status = status;
    }
}
