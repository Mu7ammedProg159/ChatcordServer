package com.mdev.chatcord.server.chat.controller;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.chat.ChatRepository;
import com.mdev.chatcord.server.chat.dto.ChatDTO;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.friend.repository.FriendRepository;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chat/privates")
@EnableMethodSecurity
public class PrivateChatController {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;
    private final ChatRepository chatRepository;

    @GetMapping("/private")
    public ResponseEntity<?> joinPrivateChat(@AuthenticationPrincipal Jwt jwt, @ModelAttribute ChatDTO chatDTO){
        User sender = userRepository.findByUuid(UUID.fromString(jwt.getClaimAsString("uuid")))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        User receiver = userRepository.findByUsernameAndTag(chatDTO.getUsername(), chatDTO.getTag()).orElseThrow(()
                -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Chat chat = new Chat(sender, );
    }

}
