package com.mdev.chatcord.server.friend.controller;

import com.mdev.chatcord.server.exception.AlreadyRegisteredException;
import com.mdev.chatcord.server.friend.dto.FriendContactDTO;
import com.mdev.chatcord.server.friend.dto.FriendDTO;
import com.mdev.chatcord.server.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/request/users/friend")
@RequiredArgsConstructor
@EnableMethodSecurity
public class FriendController {

    private final FriendService friendService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/add/{username}/{tag}")
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal Jwt jwt, @PathVariable String username, @PathVariable String tag){

        try {
            FriendDTO friendDTO = friendService.addFriend(jwt.getClaimAsString("uuid"), username, tag);
            return ResponseEntity.ok(friendDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (LockedException e){
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getMessage());
        } catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AlreadyRegisteredException e){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        }
    }

    @GetMapping("/{username}/{tag}")
    public ResponseEntity<?> requestFriend(@AuthenticationPrincipal Jwt jwt, @PathVariable String username, @PathVariable String tag){

        try {
            FriendContactDTO friendDTO = friendService.getFriend(jwt.getClaimAsString("uuid"), username, tag);
            return ResponseEntity.ok(friendDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (LockedException e){
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getMessage());
        } catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AlreadyRegisteredException e){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllFriends(@AuthenticationPrincipal Jwt jwt){

        List<FriendContactDTO> friendDTOList = friendService.getAllFriends(jwt.getClaimAsString("uuid"));

        return ResponseEntity.ok(friendDTOList);
    }

}
