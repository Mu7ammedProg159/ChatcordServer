package com.mdev.chatcord.server.direct.repository;

import com.mdev.chatcord.server.direct.model.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Long> {

    Optional<PrivateChat> findByReceiverId(Long receiver_id);
}
