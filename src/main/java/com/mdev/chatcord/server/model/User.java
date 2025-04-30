package com.mdev.chatcord.server.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long uid;

    private String username;
    private String tag;
    private String Password;
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private Set<ERoles> roles = new HashSet<>(Set.of(ERoles.USER));

    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = false;
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private EStatus status = EStatus.OFFLINE;
    private String userSocket;


    public User(String email, String password, String username) {
        this.email = email;
        Password = password;
        this.username = username;
    }
    public boolean isAccountNonLocked() {
        isAccountNonLocked = emailVerified;
        return isAccountNonLocked;
    }
}


