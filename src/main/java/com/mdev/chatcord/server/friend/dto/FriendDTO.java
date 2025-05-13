package com.mdev.chatcord.server.friend.dto;

import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.user.service.EUserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FriendDTO {
    private String username;
    private String tag;
    private String friendName;
    private String friendTag;
    private String friendPfp;
    private EFriendStatus friendStatus;
    private EUserState friendState;
    private LocalDateTime addedAt;
}
