package com.mdev.chatcord.server.communication.repository;

import com.mdev.chatcord.server.communication.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByUserId(Long id);
    Optional<ChatMember> findByChatId(Long id);

}
