package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.redis.model.MessageRedis;

public interface MessageFactory {
    MessageDTO toMessageDTO(Message message);
    Message toMessageByDTO(MessageDTO message);
    Message toMessageByRedis(MessageRedis message);
    MessageRedis toRedisMessage(Message message);
}
