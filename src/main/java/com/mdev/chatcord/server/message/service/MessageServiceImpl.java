package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ChatMemberRepository chatMemberRepository;

    private final ChatRepository chatRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProfileRepository profileRepository;

    private final RedisMessageService redisMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    private int messagesCounter = 0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(MessageDTO message) {
        Profile sender = profileRepository.findByUuid(UUID.fromString(message.getSender().getUuid())).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Profile receiver = profileRepository.findByUuid(UUID.fromString(message.getReceiver().getUuid()))
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
        DirectChat chat = (DirectChat) chatRepository.findPrivateChatBetweenUsers(sender.getId(), receiver.getId(), ChatType.PRIVATE);

        try {
            messagingTemplate.convertAndSendToUser(message.getReceiver().getUuid(), "/queue/private/message.send", message);
            message.setMessageStatus(EMessageStatus.SENT);
        } catch (Exception e){
            log.error("WARN: Couldn't send message. Check you connection.");
            message.setMessageStatus(EMessageStatus.FAILED);
        }

        try {
            messagingTemplate.convertAndSendToUser(message.getSender().getUuid(), "/queue/private/message.send", message);
        } catch (Exception e){
            log.error("The message sent, but {} couldn't receive it.", message.getReceiver().getUsername());
            message.setMessageStatus(EMessageStatus.UNDELIVERED);
        }

        message.setMessageStatus(EMessageStatus.DELIVERED);

        Message messageEntity = toMessage(message);
        redisMessageService.bufferMessage(chat.getId(), redisMessageService.toRedis(chat, messageEntity));

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageDTO setSeenMessage(MessageDTO message){
        Message messageEntity = messageRepository.findByUuid(message.getMessageUUID()).orElseThrow(() ->
                new BusinessException(ExceptionCode.MESSAGE_NOT_FOUND));

        messageEntity.setState(EMessageStatus.SEEN);
        messageEntity.setSeenAt(LocalDateTime.now());
        messageRepository.save(messageEntity);

        message.setMessageStatus(EMessageStatus.SEEN);
        message.setSeenAt(LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(message.getSender().getUuid(), "/queue/private/message.state", message);

        return message;
    }

    public Message toMessage(MessageDTO message){

        if (message == null)
            return null;

        Chat chat = chatRepository.findByUuid(message.getChatUUID()).orElseThrow(() -> new BusinessException(ExceptionCode.CHAT_NOT_FOUND));
        Profile sender = profileRepository.findByUuid(UUID.fromString(message.getSender().getUuid()))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        if (message.getReplyTo() == null)
            return new Message(message.getMessageUUID(), sender, chat, chat.getType(),
                    message.getContent(), message.getType(), null, message.isEdited(),  message.isPinned(),
                    message.getSentAt(), message.getSeenAt(), message.getMessageStatus());

        return new Message(message.getMessageUUID(), sender, chat, chat.getType(),
                message.getContent(), message.getType(), toMessage(message.getReplyTo()),
                message.isEdited(), message.isPinned(), message.getSentAt(), message.getSeenAt(),
                message.getMessageStatus());
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
                chat.setLastMessageSent(messages.stream()
                        .filter(msg -> msg.getSentAt() != null) // optional, in case of null dates
                        .max(Comparator.comparing(Message::getSentAt)).orElse(
                                messages.get(messages.size()-1)
                        ));
                chatRepository.save(chat);
            }
            redisMessageService.clearBufferedMessages(chatId);
        }
    }
}
