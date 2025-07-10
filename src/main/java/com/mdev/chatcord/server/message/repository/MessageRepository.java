package com.mdev.chatcord.server.message.repository;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.message.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findBySenderId(Long sender_id);
    Optional<Message> findByUuid(UUID uuid);

    Page<Message> findByChatIdOrderBySentAtDesc(Long chatId, Pageable pageable);

    @Query(
    """
    SELECT m FROM Message m 
    WHERE m.chat.type = :chatType
      AND m.chat.id IN (
          SELECT c.id FROM Chat c 
          JOIN c.members m1 
          JOIN c.members m2 
          WHERE (
                  (m1.user.id = :ownerId AND m2.user.id = :friendId)
                  OR
                  (m1.user.id = :friendId AND m2.user.id = :ownerId)
                )
            AND c.type = :chatType
    ORDER BY m.sentAt DESC
    """
    )
    Page<Message> findChatMessagesByFriendship(
            @Param("ownerId") Long ownerId,
            @Param("friendId") Long friendId,
            @Param("chatType") ChatType chatType,
            Pageable pageable
    );

}
