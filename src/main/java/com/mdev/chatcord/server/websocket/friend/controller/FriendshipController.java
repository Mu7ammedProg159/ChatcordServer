package com.mdev.chatcord.server.websocket.friend.controller;

import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.user.service.UserService;
import com.mdev.chatcord.server.websocket.friend.dto.FriendUser;
import com.mdev.chatcord.server.websocket.friend.service.FriendNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final SimpMessagingTemplate messagingTemplate;
    private final FriendService friendService;
    private final FriendNotifierService friendNotifierService;
    private final UserService userService;
    private final SimpUserRegistry simpUserRegistry;

    @MessageMapping("/friend.add")
    public void addFriend(Principal principal, ContactPreview receiver){
        ContactPreview requester = friendService.getFriendshipRequester(principal.getName(),
                receiver.getDisplayName(), receiver.getTag());
        friendNotifierService.addFriendshipInRealtime(requester, receiver);
    }

    @MessageMapping("/friend.update")
    public void acceptFriendship(Principal principal, FriendUser dto){
        friendNotifierService.updateFriendshipInRealtime(principal.getName(), dto.getUsername(), dto.getTag());
    }

}
// simpUserRegistry.getUsers().forEach(user -> log.info("User: {}", user.getName()));
