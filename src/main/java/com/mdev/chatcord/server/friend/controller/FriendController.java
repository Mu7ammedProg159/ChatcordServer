package com.mdev.chatcord.server.friend.controller;

import com.mdev.chatcord.server.chat.direct.service.DirectChatService;
import com.mdev.chatcord.server.friend.dto.ContactPair;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.friend.service.FriendServiceImpl;
import com.mdev.chatcord.server.token.annotation.RequiredAccessToken;
import com.mdev.chatcord.server.websocket.friend.service.FriendNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/request/users/friends")
@RequiredArgsConstructor
@EnableMethodSecurity
public class FriendController {

    private final FriendService friendService;
    private final FriendNotificationService friendNotificationService;
    private final DirectChatService directChatService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/friend/add")
    @RequiredAccessToken
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){
        String uuid = jwt.getClaimAsString("uuid");

        ContactPair participants = friendService.add(uuid, username, tag);

        friendNotificationService.addFriendshipInRealtime(participants.getRequester(), participants.getReceiver());
        return ResponseEntity.ok(participants.getReceiver());
    }

    @GetMapping("/friend")
    @RequiredAccessToken
    public ResponseEntity<?> requestFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){
        return ResponseEntity.ok(friendService.retrieveFriendship(jwt.getClaimAsString("uuid"), username, tag));
    }

    @GetMapping("/all")
    @RequiredAccessToken
    public ResponseEntity<?> getAllFriends(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(friendService.retrieveAllFriendships(jwt.getClaimAsString("uuid")));
    }

    @PutMapping("/friend/accept")
    @RequiredAccessToken
    public ResponseEntity<?> acceptFriendship(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){
        String uuid = jwt.getClaimAsString("uuid");

        ContactPair participants = friendService.accept(uuid, username, tag);
        friendNotificationService.updateFriendshipInRealtime(participants.getRequester(), participants.getReceiver());
        logger.info("User with UUID: {} successfully accepted user with: Username&Tag: {}#{}.",
                uuid, username, tag);

        return ResponseEntity.ok("Now " + username + "#" + tag + " is your friend.");
    }

    @PutMapping("/friend/decline")
    @RequiredAccessToken
    public ResponseEntity<?> declineFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){
        String uuid = jwt.getClaimAsString("uuid");

        ContactPair participants = friendService.decline(uuid, username, tag);
        friendNotificationService.updateFriendshipInRealtime(participants.getRequester(), participants.getReceiver());

        return ResponseEntity.ok("Friend with name: " + username + " has been declined successfully");
    }

}
