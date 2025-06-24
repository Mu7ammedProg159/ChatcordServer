package com.mdev.chatcord.server.websocket.friend.dto;

import com.mdev.chatcord.server.friend.dto.ContactPreview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptFriendDTO {
    String uuid;
}
