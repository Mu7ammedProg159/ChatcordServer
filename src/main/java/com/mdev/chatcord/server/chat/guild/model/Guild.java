package com.mdev.chatcord.server.chat.guild.model;

import com.mdev.chatcord.server.chat.Chat;
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
public class Guild extends Chat {

    private String guildName;
    private String description;
    private String guildPictureUrl;

    @ManyToOne
    private Account owner;

    private boolean isPublic;

    //You may add commanders etc. For roles or maybe other implementations somewhere how-where else.

}
