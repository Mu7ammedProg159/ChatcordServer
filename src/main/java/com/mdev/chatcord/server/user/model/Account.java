package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.BaseEntity;
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
public class Account extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    private String Password;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<ERoles> roles = new HashSet<>();

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = false;
    private boolean isActive = true;

    public Account(String email, String password) {
        this.email = email;
        Password = password;
    }

    public boolean isAccountNonLocked() {
        isAccountNonLocked = emailVerified;
        return isAccountNonLocked;
    }

}


