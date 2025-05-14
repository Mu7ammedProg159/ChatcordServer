package com.mdev.chatcord.server.communication.repository;

import com.mdev.chatcord.server.communication.model.UserChatRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChatRelationRepository extends JpaRepository<UserChatRelation, Long> {

    Optional<UserChatRelation> findByUserId(Long id);
    Optional<UserChatRelation> findByChatId(Long id);

}
