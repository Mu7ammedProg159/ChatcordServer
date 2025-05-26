package com.mdev.chatcord.server.chat.controller;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final AccountRepository accountRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO message){
        Chat chat = null;
        switch (message.getChatType()){
            case PRIVATE -> {
                messagingTemplate.convertAndSendToUser(message.getReceiver(), "/queue/private", message);
            }
            case GUILD -> {
                messagingTemplate.convertAndSend("/topic/group/" + message.getReceiver(), message);
            }
        }

    }

}
