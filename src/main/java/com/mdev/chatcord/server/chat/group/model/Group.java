package com.mdev.chatcord.server.chat.group.model;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.user.model.Account;
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
public class Group extends Chat {

    private String groupName;
    private String description;
    private String groupPictureUrl;

    @ManyToOne
    private Account owner;

    private boolean isPublic;

    //You may add commanders etc. For roles or maybe other implementations somewhere how-where else.

}
