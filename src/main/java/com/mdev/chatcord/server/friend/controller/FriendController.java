package com.mdev.chatcord.server.friend.controller;

import com.mdev.chatcord.server.friend.dto.FriendDTO;
import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.friend.repository.FriendRepository;
import com.mdev.chatcord.server.friend.service.EFriendStatus;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@EnableMethodSecurity
public class FriendController {

    private final FriendService friendService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/users/{username}/{tag}")
    public ResponseEntity<?> getFriend(Authentication authentication, @PathVariable String username, @PathVariable String tag){

        try {
            FriendDTO friendDTO = friendService.addFriend(authentication.getName(), username, tag);
            return ResponseEntity.ok(friendDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (LockedException e){
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getMessage());
        } catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AlreadyBuiltException e){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        }
    }

}
