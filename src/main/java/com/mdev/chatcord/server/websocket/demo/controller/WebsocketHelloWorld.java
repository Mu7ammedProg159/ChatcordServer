package com.mdev.chatcord.server.websocket.demo.controller;

import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.websocket.demo.dto.MessagesDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@Slf4j
public class WebsocketHelloWorld {
    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void receiveMessage(MessagesDTO message, Principal principal) {
        log.info("Session connected as: {}", principal.getName()); // should be UUID

        Profile from = profileRepository.findByUuid(UUID.fromString(message.getFrom())).orElseThrow();
        Profile to = profileRepository.findByUuid(UUID.fromString(message.getTo())).orElseThrow();

        log.info("User {} sent message to {}: {}.", from.getUsername(), to.getUsername(), message.getContent());
        //messagingTemplate.convertAndSend("/topic/messages", message);
        messagingTemplate.convertAndSendToUser(message.getTo(), "/queue/messages", message);
    }
}