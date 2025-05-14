package com.mdev.chatcord.server.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Chat findByCreatedAtAfter(LocalDateTime dateTime);
}
