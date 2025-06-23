package com.mdev.chatcord.server.websocket.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddFriendDTO {
    private String username;
    private String tag;
}
