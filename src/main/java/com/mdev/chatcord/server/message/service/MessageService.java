package com.mdev.chatcord.server.message.service;

import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Queue;

public interface MessageService {
    void send(MessageDTO messageDTO);
    void save();
}
