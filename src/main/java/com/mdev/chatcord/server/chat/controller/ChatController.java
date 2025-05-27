package com.mdev.chatcord.server.chat.controller;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.chat.ChatRepository;
import com.mdev.chatcord.server.chat.ChatType;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final AccountRepository accountRepository;
    private final ChatRepository chatRepository;
    private final ProfileRepository profileRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO message, org.springframework.messaging.Message<?> wsMessage, Principal principal){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(wsMessage);

        Profile owner = profileRepository.findByUuid(message.getSender())
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Profile receiver = profileRepository.findByUuid(message.getReceiver())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Chat chat = chatRepository.findPrivateChatBetweenUsers(owner.getId(), receiver.getId(), ChatType.PRIVATE)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CHAT_NOT_FOUND));

        switch (message.getChatType()){
            case PRIVATE -> {
                messagingTemplate.convertAndSendToUser(String.valueOf(message.getReceiver()), "/queue/private", message);
                Message messageEntity = new Message(owner, chat,
                        message.getContent(), LocalDateTime.now(), null, message.getMessageStatus());
                chat.getMessages().add(messageEntity);
                messageEntity.setChat(chat);

                chatRepository.save(chat);
            }
            case GUILD -> {
                messagingTemplate.convertAndSend("/topic/group/" + message.getReceiver(), message);
            }
        }

    }

}
