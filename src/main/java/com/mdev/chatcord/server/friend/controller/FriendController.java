package com.mdev.chatcord.server.friend.controller;

import com.mdev.chatcord.server.chat.direct.dto.PrivateChatDTO;
import com.mdev.chatcord.server.chat.direct.service.DirectChatService;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.token.annotation.RequiredAccessToken;
import com.mdev.chatcord.server.token.model.TokenType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
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
    private final DirectChatService directChatService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/friend/add")
    @RequiredAccessToken
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){

        ContactPreview contactPreview = friendService.addFriend(jwt.getClaimAsString("uuid"), username, tag);
        return ResponseEntity.ok(contactPreview);
    }

    @GetMapping("/friend")
    @RequiredAccessToken
    public ResponseEntity<?> requestFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){

        ContactPreview contactPreview = friendService.getFriendship(jwt.getClaimAsString("uuid"), username, tag);
        return ResponseEntity.ok(contactPreview);
    }

//    @GetMapping("/pending/all")
//    public ResponseEntity<?> retrieveAllPendingFriends(@AuthenticationPrincipal Jwt jwt) {
//        List<ContactPreview> pendingFriends = friendService.getAllRequestedFriends(jwt.getClaimAsString("uuid"));
//        return ResponseEntity.ok(pendingFriends);
//    }

    @GetMapping("/all")
    @RequiredAccessToken
    public ResponseEntity<?> getAllFriends(@AuthenticationPrincipal Jwt jwt){

        List<ContactPreview> contacts = friendService.getAllFriends(jwt.getClaimAsString("uuid"));

        return ResponseEntity.ok(contacts);
    }

    @DeleteMapping("/friend/remove")
    @RequiredAccessToken
    public ResponseEntity<?> deleteFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){
        friendService.removeFriend(jwt.getClaimAsString("uuid"), username, tag);
        return ResponseEntity.ok("Friend with name: " + username + " has been deleted successfully");
    }

}
