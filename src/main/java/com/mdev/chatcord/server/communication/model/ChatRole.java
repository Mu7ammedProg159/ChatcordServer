package com.mdev.chatcord.server.communication.model;

import com.mdev.chatcord.server.chat.core.model.Chat;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "chat_id"}))
public class ChatRole {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Role name

    @ManyToMany
    private Set<Privilege> privileges; // What you can do with this Role ?

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat; // Where this role is defined ?

    public ChatRole(String name, Set<Privilege> privileges, Chat chat) {
        this.name = name;
        this.privileges = privileges;
        this.chat = chat;
    }
}
