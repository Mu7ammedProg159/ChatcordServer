package com.mdev.chatcord.server.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByMembers_User_Id(Long id);

    @Query("""
    SELECT c FROM Chat c
    WHERE c.type = :type
      AND SIZE(c.members) = 2
      AND EXISTS (
        SELECT m1 FROM ChatMember m1 WHERE m1.chat = c AND m1.account.id = :senderId
      )
      AND EXISTS (
        SELECT m2 FROM ChatMember m2 WHERE m2.chat = c AND m2.account.id = :friendId
      )
   """)
    Optional<Chat> findPrivateChatBetweenUsers(@Param("senderId") Long senderId,
                                               @Param("friendId") Long friendId,
                                               @Param("type") ChatType type);

    Chat findByCreatedAtAfter(LocalDateTime dateTime);

}
