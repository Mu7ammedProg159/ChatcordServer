package com.mdev.chatcord.server.websocket.chat.contorller;

import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

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
