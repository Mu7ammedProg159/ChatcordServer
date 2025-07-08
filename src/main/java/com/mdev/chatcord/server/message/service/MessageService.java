package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.message.dto.MessageDTO;

public interface MessageService {
    void send(MessageDTO messageDTO);
    MessageDTO setSeenMessage(MessageDTO message);
    void save();
}
