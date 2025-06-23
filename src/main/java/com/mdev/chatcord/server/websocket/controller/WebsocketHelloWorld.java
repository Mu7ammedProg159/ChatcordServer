package com.mdev.chatcord.server.websocket.controller;

import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.UUID;

// === BACKEND: Spring Boot WebSocket Server ===
// WebSocketConfig.java

// MessageDTO.java
@ToString
@Getter
@Setter
class MessagesDTO {
    private String from;
    private String to;
    private String content;

    // Constructors, getters, setters
    public MessagesDTO() {}
    public MessagesDTO(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }

}

// ChatController.java
@Controller
@Slf4j
class ChatsController {
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