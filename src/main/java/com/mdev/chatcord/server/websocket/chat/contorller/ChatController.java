package com.mdev.chatcord.server.websocket.chat.contorller;

import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.chat.direct.service.DirectChatService;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.message.service.MessageService;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    @MessageMapping("/direct/message.send")
    public void sendMessage(Principal principal, MessageDTO message){

        if (!principal.getName().equalsIgnoreCase(message.getSender().getUuid()))
            throw new BusinessException(ExceptionCode.INVALID_SENDER);

        switch (message.getChatType()){
            case PRIVATE -> {
                messageService.send(message);
                log.info("{} sent message to {} with content: {}.", message.getSender().getUsername(),
                        message.getReceiver().getUsername(), message.getContent());
            }
            case GUILD -> {
            }
        }

    }

}
