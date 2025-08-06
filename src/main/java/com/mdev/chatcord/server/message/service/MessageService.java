package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.chat.direct.dto.MessageStatusDTO;
import com.mdev.chatcord.server.message.dto.MessageDTO;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    void send(MessageDTO messageDTO);
    void updateMessageStatus(MessageStatusDTO dto);
    List<MessageDTO> setAllMessagesStatus(UUID chatId, UUID receiverUUID, EMessageStatus status);
    void save();
}
