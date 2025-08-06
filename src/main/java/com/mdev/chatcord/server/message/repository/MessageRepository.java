package com.mdev.chatcord.server.message.repository;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.service.EMessageStatus;
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
    //Page<Message> findAllByChat_UuidAndMessageStatus(UUID chatUuid, EMessageStatus status, Pageable pageable);

    @Query("SELECT m FROM Message m " +
            "WHERE m.chat.uuid = :chatUuid " +
            "AND m.sender.uuid <> :receiverUuid " +
            "AND m.state = :state")
    Page<Message> findMessagesForChatExceptSenderWithState(@Param("chatUuid") UUID chatUuid,
                                                           @Param("receiverUuid") UUID receiverUuid,
                                                           @Param("state") EMessageStatus state,
                                                           Pageable pageable);

    Page<Message> findAllByChat_UuidAndSender_UuidNotAndState(
            UUID chatUuid,
            UUID senderUuid,
            EMessageStatus state,
            Pageable pageable
    );

    Page<Message> findByChatIdOrderBySentAtAsc(Long chatId, Pageable pageable);

    @Query("""
    SELECT m FROM Message m
    JOIN FETCH m.chat c
    JOIN FETCH m.sender s
    WHERE m.chat.type = :chatType
      AND m.chat.id IN (
          SELECT c.id FROM Chat c
          JOIN c.members m1
          JOIN c.members m2
          WHERE (
              (m1.profile.id = :ownerId AND m2.profile.id = :friendId)
              OR
              (m1.profile.id = :friendId AND m2.profile.id = :ownerId)
          )
          AND c.type = :chatType
      )
    ORDER BY m.sentAt ASC
""")
    Page<Message> findChatMessagesByFriendship(
            @Param("ownerId") Long ownerId,
            @Param("friendId") Long friendId,
            @Param("chatType") ChatType chatType,
            Pageable pageable
    );

}
