package com.mdev.chatcord.server.chat.community.model;

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
public class Community extends Chat {

    private String description;
    private boolean isPublic;

    //You may add commanders etc. For roles or maybe other implementations somewhere how-where else.

}
