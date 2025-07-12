package com.mdev.chatcord.server.message.controller;

import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/request/messages")
@RequiredArgsConstructor
@EnableMethodSecurity
public class MessageController {

    private final MessageService messageService;

    @PutMapping("/message/direct/status/seen")
    public ResponseEntity<?> updateMessageStatusSeen(@AuthenticationPrincipal Jwt jwt, @RequestBody MessageDTO message){
        MessageDTO updatedMessage = messageService.setSeenMessage(message);
        return ResponseEntity.ok(updatedMessage);
    }

    @PutMapping("/direct/status")
    public ResponseEntity<?> updateMessageStatusSeen(@AuthenticationPrincipal Jwt jwt, @RequestParam EMessageStatus status,
                                                     @RequestParam String chatId){
        UUID receiverUUID = jwt.getClaim("uuid");
        MessageDTO updatedMessage = messageService.setAllMessagesStatus(UUID.fromString(chatId), receiverUUID, status);
        return ResponseEntity.ok(updatedMessage);
    }

}
