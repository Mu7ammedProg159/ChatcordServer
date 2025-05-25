package com.mdev.chatcord.server.friend.dto;

import com.mdev.chatcord.server.friend.service.EFriendStatus;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FriendContactDTO {

    private String name;
    private String tag;
    private String profilePictureURL;
    private EFriendStatus friendStatus;

}
