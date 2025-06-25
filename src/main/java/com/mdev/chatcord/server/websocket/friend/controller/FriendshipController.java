package com.mdev.chatcord.server.websocket.friend.controller;

import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.service.UserService;
import com.mdev.chatcord.server.websocket.friend.dto.FriendUser;
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
    private final UserService userService;
    private final SimpUserRegistry simpUserRegistry;

    @MessageMapping("/friend.add")
    public void addFriend(FriendUser dto, Principal principal){

        ContactPreview contactPreview = friendService.getFriendshipRequester(principal.getName(), dto.getUsername(), dto.getTag());
        ProfileDetails friend = userService.getUserProfileByUsernameAndTag(dto.getUsername(), dto.getTag());

        log.info("{} with uuid: {} requested friendship with {} of uuid: {}",
                contactPreview.getDisplayName(),
                principal.getName(),
                friend.getUsername(),
                friend.getUuid().toLowerCase());

        messagingTemplate.convertAndSendToUser(
                friend.getUuid().toLowerCase(),
                "/queue/friendship.add",
                contactPreview);
    }

    @MessageMapping("/friend.update")
    public void acceptFriendship(Principal principal, FriendUser dto){
        ProfileDetails user = userService.getUserProfile(principal.getName());
        ProfileDetails friend = userService.getUserProfileByUsernameAndTag(dto.getUsername(), dto.getTag());
        friendService.updateFriendshipInRealtime(friend, user);
    }

}
// simpUserRegistry.getUsers().forEach(user -> log.info("User: {}", user.getName()));
