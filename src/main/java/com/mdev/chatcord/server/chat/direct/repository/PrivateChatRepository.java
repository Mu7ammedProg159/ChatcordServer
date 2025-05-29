package com.mdev.chatcord.server.chat.direct.repository;

import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PrivateChatRepository extends JpaRepository<DirectChat, Long> {

    Optional<DirectChat> findByReceiverId(Long id);

}
