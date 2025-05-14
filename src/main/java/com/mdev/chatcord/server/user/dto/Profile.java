package com.mdev.chatcord.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Profile {

    private String email;
    private String username;
    private String tag;
    private String status;
    private String pfpUrl;
    private String about;
    private String quote;

}
