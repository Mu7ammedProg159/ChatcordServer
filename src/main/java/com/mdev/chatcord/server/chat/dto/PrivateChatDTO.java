package com.mdev.chatcord.server.chat.dto;

import com.mdev.chatcord.server.friend.dto.FriendContactDTO;
import com.mdev.chatcord.server.friend.dto.FriendDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrivateChatDTO {
    FriendContactDTO friendContactDTO;
    ChatDTO chatDTO;
}
