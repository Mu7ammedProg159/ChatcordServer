package com.mdev.chatcord.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private ERoles role;

    private EStatus status = EStatus.OFFLINE;
    private String userSocket;
}
