package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.authentication.service.ERoles;
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
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    private String email;
    private String Password;

    private String username;

    @Column(unique = true, nullable = false)
    private String tag = generateTag();


    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private Set<ERoles> roles = new HashSet<>(Set.of(ERoles.USER));

    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = false;
    private boolean isActive = true;

    public User(String email, String password, String username) {
        this.email = email;
        Password = password;
        this.username = username;
    }

    public boolean isAccountNonLocked() {
        isAccountNonLocked = emailVerified;
        return isAccountNonLocked;
    }

    public String generateTag(){
        Random random = new Random();
        int id = random.nextInt(9000) + 1000;
        return String.valueOf(id);
    }
}


