package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.redis.model.MessageRedis;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageFactoryImpl implements MessageFactory{

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;

    @Override
    public MessageDTO toMessageDTO(Message message) {
        if (message == null)
            return null;

        Profile entitySender = message.getSender();
        Profile entityReceiver = message.getChat().getMembers().get(0).getProfile();

        ChatMemberDTO sender = new ChatMemberDTO(entitySender.getUuid().toString().toLowerCase(),
                entitySender.getUsername(), entitySender.getTag(), entitySender.getAvatarUrl(),
                entitySender.getAvatarHexColor(), null);
        ChatMemberDTO receiver = new ChatMemberDTO(entityReceiver.getUuid().toString().toLowerCase(),
                entityReceiver.getUsername(), entityReceiver.getTag(), entityReceiver.getAvatarUrl(),
                entityReceiver.getAvatarHexColor(), null);

        if (message.getReplyTo() == null)
            return new MessageDTO(message.getUuid(), message.getChat().getUuid(), message.getChat().getType(),
                    message.getMessage(), message.getType(), null, sender, receiver,
                    message.getSentAt(), message.getSeenAt(), message.isEdited(), message.isPinned(), message.getState());;

        return new MessageDTO(message.getUuid(), message.getChat().getUuid(), message.getChat().getType(),
                message.getMessage(), message.getType(), toMessageDTO(message.getReplyTo()), sender, receiver,
                message.getSentAt(), message.getSeenAt(), message.isEdited(), message.isPinned(), message.getState());
    }

    @Override
    public Message toMessageByDTO(MessageDTO message) {
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
                message.getContent(), message.getType(), toMessageByDTO(message.getReplyTo()),
                message.isEdited(), message.isPinned(), message.getSentAt(), message.getSeenAt(),
                message.getMessageStatus());
    }

    @Override
    public Message toMessageByRedis(MessageRedis message){
        Message replyTo = Optional.ofNullable(message.getReplyToId())
                .flatMap(messageRepository::findById)
                .orElse(null);
        Chat chat = chatRepository.findByUuid(message.getChatUUID()).orElseThrow(() -> new BusinessException(ExceptionCode.CHAT_NOT_FOUND));
        return new Message(message.getUuid(), message.getSender(), chat, message.getContext(), message.getContent(), message.getType(),
                replyTo, message.isEdited(), message.isPinned(), message.getSentAt(), message.getSeenAt(), message.getMessageState());
    }

    @Override
    public MessageRedis toRedisMessage(Message messageEntity) {
        return new MessageRedis(messageEntity.getId(), messageEntity.getChat().getId(), messageEntity.getUuid(),
                messageEntity.getChat().getUuid(), messageEntity.getContext(), messageEntity.getMessage(),
                messageEntity.getType(), messageEntity.getReplyTo() != null ? messageEntity.getReplyTo().getId() : null,
                messageEntity.getSender(), messageEntity.getSentAt(), messageEntity.getSeenAt(), messageEntity.isEdited(),
                messageEntity.isPinned(), messageEntity.getState());
    }
}
