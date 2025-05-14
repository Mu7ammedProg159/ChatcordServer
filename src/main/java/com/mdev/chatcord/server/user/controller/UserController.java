package com.mdev.chatcord.server.user.controller;

import com.mdev.chatcord.server.user.dto.Profile;
import com.mdev.chatcord.server.user.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/users/me")
    public ResponseEntity<Profile> getUserProfile(Authentication authentication){

        Profile profileDTO = userService.getUserProfile(authentication.getName());

        return ResponseEntity.ok(profileDTO);
    }

}
