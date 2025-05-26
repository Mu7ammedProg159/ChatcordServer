package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Profile extends BaseEntity {

    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Account account;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    private String username;

    @Column(unique = true, nullable = false)
    private String tag = generateTag();

    private String avatarUrl;
    private String quote;
    private String aboutMe;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserStatus userStatus;

    public Profile(String username, String avatarUrl, String quote, String aboutMe) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.quote = quote;
        this.aboutMe = aboutMe;
    }

    //    public UserProfile(Account account, String quote, String profilePictureUrl, String aboutMe) {
//        this.account = account;
//        this.quote = quote;
//        this.avatarUrl = profilePictureUrl;
//        this.aboutMe = aboutMe;
//    }

    public String generateTag(){
        Random random = new Random();
        int id = random.nextInt(9000) + 1000;
        return String.valueOf(id);
    }
}
