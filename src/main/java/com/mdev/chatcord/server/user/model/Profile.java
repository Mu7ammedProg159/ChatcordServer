package com.mdev.chatcord.server.user.model;

import com.mdev.chatcord.server.common.BaseEntity;
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

    @OneToOne(mappedBy = "profile")
    private Account account;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    private String username;

    @Column(unique = true, nullable = false)
    private String tag = generateTag();

    private String avatarUrl;
    private String avatarHexColor = generateRandomDarkishColor();
    private String quote;
    private String aboutMe;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
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

    public static String generateRandomDarkishColor() {
        Random RANDOM = new Random();
        float hue = RANDOM.nextFloat(); // full 0.0 to 1.0 range
        float saturation = 0.5f + RANDOM.nextFloat() * 0.4f; // 0.5 to 0.9
        float brightness = 0.4f + RANDOM.nextFloat() * 0.4f; // 0.4 to 0.8 (avoids white)

        int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return String.format("#%06X", (rgb & 0xFFFFFF));
    }

}
