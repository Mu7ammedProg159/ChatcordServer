package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.message.dto.MessageDTO;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    void send(MessageDTO messageDTO);
    MessageDTO setSeenMessage(MessageDTO message);
    List<MessageDTO> setAllMessagesStatus(UUID chatId, UUID receiverUUID, EMessageStatus status);
    void save();
}
