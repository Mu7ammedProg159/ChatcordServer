package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.redis.model.MessageRedis;
import com.mdev.chatcord.server.redis.service.RedisMessageService;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final ChatRepository chatRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProfileRepository profileRepository;

    private final RedisMessageService redisMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    private int messagesCounter = 0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(MessageDTO message) {
        Profile sender = profileRepository.findByUuid(UUID.fromString(message.getSender().getUuid())).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Profile receiver = profileRepository.findByUuid(UUID.fromString(message.getReceiver().getUuid()))
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
        DirectChat chat = (DirectChat) chatRepository.findPrivateChatBetweenUsers(sender.getId(), receiver.getId(), ChatType.PRIVATE);

        messagingTemplate.convertAndSendToUser(message.getReceiver().getUuid(), "/queue/private/message.send", message);
        messagingTemplate.convertAndSendToUser(message.getSender().getUuid(), "/queue/private/message.send", message);

        Message messageEntity = new Message(sender, chat, message.getContent(), LocalDateTime.now(), null,
               message.isEdited(), message.getMessageStatus());
        redisMessageService.bufferMessage(chat.getId(), redisMessageService.toRedis(chat, messageEntity));

    }

    @Override
    @Scheduled(fixedRate = 120_000)
    @Transactional(rollbackFor = Exception.class)
    public void save(){
        Set<Long> chatIds = redisMessageService.getAllBufferedChatIds();
        for (Long chatId : chatIds) {
            List<Message> messages = redisMessageService.getBufferedMessages(chatId);
            if (messages == null || messages.isEmpty()) continue;

            DirectChat chat = (DirectChat) chatRepository.findById(chatId).orElse(null);
            if (chat != null) {
                log.info("{} messages have been registered in database.", messages.size());
                chat.getMessages().addAll(messages);
                chatRepository.save(chat);
            }
            redisMessageService.clearBufferedMessages(chatId);
        }
    }
}
