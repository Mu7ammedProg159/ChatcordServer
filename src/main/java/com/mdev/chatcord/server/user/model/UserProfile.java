package com.mdev.chatcord.server.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private User user;

    private String quote;
    private String profilePictureUrl;
    private String aboutMe;

    public UserProfile(User user, String quote, String profilePictureUrl, String aboutMe) {
        this.user = user;
        this.quote = quote;
        this.profilePictureUrl = profilePictureUrl;
        this.aboutMe = aboutMe;
    }
}
