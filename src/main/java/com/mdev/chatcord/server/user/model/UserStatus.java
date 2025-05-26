package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.BaseEntity;
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
public class UserStatus extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private EUserState status;

    @OneToOne
    private Profile profile;

    public UserStatus(EUserState status) {
        this.status = status;
    }
}
