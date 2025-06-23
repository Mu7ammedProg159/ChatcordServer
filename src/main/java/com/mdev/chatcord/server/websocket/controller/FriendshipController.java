package com.mdev.chatcord.server.websocket.controller;

import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.user.service.UserService;
import com.mdev.chatcord.server.websocket.dto.AddFriendDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;

@RequiredArgsConstructor
public class FriendshipController {

    private final SimpMessagingTemplate messagingTemplate;
    private final FriendService friendService;
    private final UserService userService;

    @MessageMapping("/add")
    public void addFriend(AddFriendDTO dto, Principal principal){
        ContactPreview contactPreview = friendService.getFriendship(principal.getName(), dto.getUsername(), dto.getTag());
        if (contactPreview == null)
            throw new BusinessException(ExceptionCode.FRIEND_NOT_FOUND);
        messagingTemplate.convertAndSendToUser(String.valueOf(contactPreview.getUuid()), "/queue/friendship", contactPreview);

    }

}
