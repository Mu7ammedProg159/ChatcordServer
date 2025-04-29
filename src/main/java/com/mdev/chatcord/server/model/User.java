package com.mdev.chatcord.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long uid;

    private String username;
    private String tag;
    private String Password;
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private Set<ERoles> roles = new HashSet<>(Set.of(ERoles.USER));

    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = true;
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private EStatus status = EStatus.OFFLINE;
    private String userSocket;

    public User(String email, String password, String username) {
        this.email = email;
        Password = password;
        this.username = username;
    }
}


