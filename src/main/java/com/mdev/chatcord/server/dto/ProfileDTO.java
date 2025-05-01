package com.mdev.chatcord.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileDTO {
    private String email;
    private String username;
    private String tag;
    private String status;
    private String userSocket;
    private boolean isEmailVerified;

}
