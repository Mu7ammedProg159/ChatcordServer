package com.mdev.chatcord.server.chat.guild.model;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Guild extends Chat {

    private String guildName;
    private String description;

    @ManyToOne
    private User owner;

    //You may add commanders etc for roles or maybe other implementations somewhere how-where else.

    @ManyToMany
    private Set<User> members;

}
