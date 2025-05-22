package com.mdev.chatcord.server.communication.repository;

import com.mdev.chatcord.server.communication.model.ChatRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoleRepository extends JpaRepository<ChatRole, Long> {
}