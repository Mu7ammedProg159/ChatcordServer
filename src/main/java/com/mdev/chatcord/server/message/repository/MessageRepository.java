package com.mdev.chatcord.server.message.repository;

import com.mdev.chatcord.server.message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findBySenderId(Long sender_id);

}
