package com.mdev.chatcord.server.websocket.friend.service;


import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FriendInteractionService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final FriendService friendService;

    public void addFriendshipInRealtime(String uuid, String username, String tag){
        ContactPreview contactPreview = friendService.getFriendshipRequester(uuid, username, tag);
        ProfileDetails friend = userService.getUserProfileByUsernameAndTag(username, tag);

        log.info("{} with uuid: {} requested friendship with {} of uuid: {}",
                contactPreview.getDisplayName(),
                uuid,
                friend.getUsername(),
                friend.getUuid().toLowerCase());

        messagingTemplate.convertAndSendToUser(
                friend.getUuid().toLowerCase(),
                "/queue/friendship.add",
                contactPreview);
    }

    public void updateFriendshipInRealtime(String uuid, String username, String tag) {

        ProfileDetails acceptor = userService.getUserProfile(uuid);
        ProfileDetails owner = userService.getUserProfileByUsernameAndTag(username, tag);

        ContactPreview contactPreview = friendService.getFriendship(owner.getUuid().toLowerCase(), acceptor.getUsername(),
                acceptor.getTag());

        log.info("{} with uuid: {} {} friendship with {} of uuid: {}",
                acceptor.getUsername(),
                acceptor.getUuid().toLowerCase(),
                contactPreview.getFriendStatus().name().toLowerCase(),
                owner.getUsername(),
                owner.getUuid().toLowerCase());

        messagingTemplate.convertAndSendToUser(
                owner.getUuid().toLowerCase(),
                "/queue/friendship.update",
                contactPreview);
    }
}
