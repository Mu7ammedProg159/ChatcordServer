package com.mdev.chatcord.server.communication.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatRole {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Role name

    @ManyToMany
    private Set<Privilege> privileges; // What you can do with this Role ?

    public ChatRole(String name, Set<Privilege> privileges) {
        this.name = name;
        this.privileges = privileges;
    }
}
