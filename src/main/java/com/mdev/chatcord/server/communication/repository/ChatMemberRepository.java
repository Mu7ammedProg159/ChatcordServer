package com.mdev.chatcord.server.communication.repository;

import com.mdev.chatcord.server.communication.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByUserId(Long id);
    Optional<ChatMember> findByChatId(Long id);

}
