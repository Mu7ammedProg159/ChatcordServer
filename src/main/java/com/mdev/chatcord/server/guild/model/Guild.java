package com.mdev.chatcord.server.guild.model;

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
public class Guild {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String guildName;
    private String description;

    @ManyToMany
    private Set<User> members;

    @OneToMany
    private Set<Message> messages;

    private int CONNECTION_SOCKET;

}
