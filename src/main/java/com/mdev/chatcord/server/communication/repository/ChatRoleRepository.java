package com.mdev.chatcord.server.communication.repository;

import com.mdev.chatcord.server.communication.model.ChatRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoleRepository extends JpaRepository<ChatRole, Long> {

    Optional<ChatRole> findByNameAndChat_Id(String name, Long chatId);

    boolean existsByNameAndChat_Id(String name, Long chatId);
}