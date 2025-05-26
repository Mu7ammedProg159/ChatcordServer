package com.mdev.chatcord.server.friend.controller;

import com.mdev.chatcord.server.chat.dto.PrivateChatDTO;
import com.mdev.chatcord.server.chat.service.PrivateChatService;
import com.mdev.chatcord.server.friend.service.FriendService;
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
@RequestMapping("/api/request/users/friend")
@RequiredArgsConstructor
@EnableMethodSecurity
public class FriendController {

    private final FriendService friendService;
    private final PrivateChatService privateChatService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/add/{username}/{tag}")
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal Jwt jwt, @PathVariable String username, @PathVariable String tag){

        PrivateChatDTO privateChatDTO = friendService.addFriend(jwt.getClaimAsString("uuid"), username, tag);
        return ResponseEntity.ok(privateChatDTO);
    }

    @GetMapping("/{username}/{tag}")
    public ResponseEntity<?> requestFriend(@AuthenticationPrincipal Jwt jwt, @PathVariable String username, @PathVariable String tag){
        PrivateChatDTO privateChatDTO = friendService.getFriendship(jwt.getClaimAsString("uuid"), username, tag);
        return ResponseEntity.ok(privateChatDTO);
    }

    @GetMapping("/pending/all")
    public ResponseEntity<?> retrieveAllPendingFriends(@AuthenticationPrincipal Jwt jwt) {
        List<PrivateChatDTO> pendingFriends = friendService.getAllPendingFriends(jwt.getClaimAsString("uuid"));
        return ResponseEntity.ok(pendingFriends);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFriends(@AuthenticationPrincipal Jwt jwt){

        List<PrivateChatDTO> friendDTOList = friendService.getAllFriends(jwt.getClaimAsString("uuid"));

        return ResponseEntity.ok(friendDTOList);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> deleteFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag){
        friendService.removeFriend(jwt.getClaimAsString("uuid"), username, tag);
        return ResponseEntity.ok("Friend with name: " + username + " has been deleted successfully");
    }

}
