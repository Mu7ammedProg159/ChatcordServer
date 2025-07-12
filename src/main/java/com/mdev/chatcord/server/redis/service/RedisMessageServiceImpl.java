package com.mdev.chatcord.server.redis.service;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.message.service.MessageFactory;
import com.mdev.chatcord.server.redis.model.MessageRedis;
import com.mdev.chatcord.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMessageServiceImpl implements RedisMessageService{
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageFactory messageFactory;

    private final RedisTemplate<String, MessageRedis> redisTemplate;
    private final UserService userService;
    private static final String PREFIX = "directChat:messages:";

    @Override
    public void bufferMessage(Long chatId, MessageRedis message) {
        String key = getKey(chatId);
        redisTemplate.opsForList().rightPush(key, message);
    }

    @Override
    public List<Message> getBufferedMessages(Long chatId) {
        String key = getKey(chatId);
        List<MessageRedis> messageRedis = getBufferedRedisMessages(chatId);
        List<Message> messages = new ArrayList<>();
        for (MessageRedis message : messageRedis){
            messages.add(messageFactory.toMessageByRedis(message));
        }
        return messages;
    }

    private List<MessageRedis> getBufferedRedisMessages(Long chatId) {
        String key = getKey(chatId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    @Override
    public void clearBufferedMessages(Long chatId) {
        String key = getKey(chatId);
        redisTemplate.delete(key);
    }

    @Override
    public Set<Long> getAllBufferedChatIds() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys.isEmpty())
            return Collections.emptySet();

        return keys.stream()
                .map(k -> Long.valueOf(k.replace(PREFIX, "")))
                .collect(Collectors.toSet());
    }

    private String getKey(Long chatId){
        return PREFIX + chatId;
    }

}
