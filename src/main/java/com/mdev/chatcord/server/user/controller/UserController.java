package com.mdev.chatcord.server.user.controller;

import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.token.model.TokenType;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/users/me")
    public ResponseEntity<ProfileDetails> getUserProfile(@AuthenticationPrincipal Jwt jwt){

        if (!jwt.getClaim("type").equals(TokenType.ACCESS)){
            throw new BusinessException(ExceptionCode.INVALID_ACCESS_TOKEN);
        }

        ProfileDetails profileDetailsDTO = userService.getUserProfile(jwt.getClaimAsString("uuid"));

        return ResponseEntity.ok(profileDetailsDTO);
    }

}
