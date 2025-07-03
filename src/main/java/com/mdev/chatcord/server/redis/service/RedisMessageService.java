package com.mdev.chatcord.server.redis.service;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.redis.model.MessageRedis;

import java.util.List;
import java.util.Set;

public interface RedisMessageService {
    void bufferMessage(Long chatId, MessageRedis message);
    List<Message> getBufferedMessages(Long chatId);
    void clearBufferedMessages(Long chatId);
    Set<Long> getAllBufferedChatIds();
    Message fromEntity(Chat chat, MessageRedis message);
    MessageRedis toRedis(DirectChat chat, Message messageEntity);
}