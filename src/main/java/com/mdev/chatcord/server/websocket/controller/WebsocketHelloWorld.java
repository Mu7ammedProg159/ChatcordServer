package com.mdev.chatcord.server.websocket.controller;

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

// === BACKEND: Spring Boot WebSocket Server ===
// WebSocketConfig.java

// MessageDTO.java
@ToString
class MessagesDTO {
    private String from;
    private String content;

    // Constructors, getters, setters
    public MessagesDTO() {}
    public MessagesDTO(String from, String content) {
        this.from = from;
        this.content = content;
    }
    public String getFrom() { return from; }
    public String getContent() { return content; }
    public void setFrom(String from) { this.from = from; }
    public void setContent(String content) { this.content = content; }
}

// ChatController.java
@Controller
@Slf4j
class ChatsController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void receiveMessage(MessagesDTO message) {
        log.info(message.toString());
        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}