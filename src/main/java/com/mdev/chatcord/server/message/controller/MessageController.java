package com.mdev.chatcord.server.message.controller;

import com.mdev.chatcord.server.chat.direct.dto.MessageStatusDTO;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.MessageService;
import com.mdev.chatcord.server.token.annotation.RequiredAccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PutMapping("/direct/message/status")
    @RequiredAccessToken
    public ResponseEntity<?> updateMessageStatus(@AuthenticationPrincipal Jwt jwt, @RequestBody MessageStatusDTO dto){
        messageService.updateMessageStatus(dto);
        return ResponseEntity.ok("Message has been updated.");
    }

    @PutMapping("/direct/status")
    @RequiredAccessToken
    public ResponseEntity<?> updateAllMessageStatus(@AuthenticationPrincipal Jwt jwt, @RequestParam String status,
                                                    @RequestParam String chatId){
        String receiverUUID = jwt.getClaimAsString("uuid");
        List<MessageDTO> updatedMessage = messageService.setAllMessagesStatus(UUID.fromString(chatId), UUID.fromString(receiverUUID), EMessageStatus.valueOf(status));
        return ResponseEntity.ok(updatedMessage);
    }

}
