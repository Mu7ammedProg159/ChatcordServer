package com.mdev.chatcord.server.friend.dto;

import lombok.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FriendContactDTO {

    private String name;
    private String tag;
    private String profilePictureURL;

    // checks the private chat if exists with the id of this friend and fetch.
    private String lastMessageSent;
    private LocalDateTime LastMessageSendDate;

}
