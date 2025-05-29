package com.mdev.chatcord.server.chat.core.repository;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
    SELECT c FROM GroupChat c
    JOIN FETCH c.members m
    WHERE m.profile.id = :profileId
    """)
    List<Group> findAllGroupChatsByProfileId(@Param("profileId") Long profileId);

    @Query("""
    SELECT c FROM Chat c
    JOIN FETCH c.members m
    WHERE c.type = :type
      AND SIZE(c.members) = 2
      AND :senderId IN (SELECT m1.profile.id FROM ChatMember m1 WHERE m1.chat = c)
      AND :friendId IN (SELECT m2.profile.id FROM ChatMember m2 WHERE m2.chat = c)
    """)
    Optional<Chat> findPrivateChatBetweenUsers(@Param("senderId") Long senderId,
                                               @Param("friendId") Long friendId,
                                               @Param("type") ChatType type);

    Chat findByCreatedAtAfter(LocalDateTime dateTime);

}
