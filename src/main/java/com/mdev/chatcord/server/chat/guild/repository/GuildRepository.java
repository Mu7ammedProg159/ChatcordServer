package com.mdev.chatcord.server.chat.guild.repository;

import com.mdev.chatcord.server.chat.guild.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuildRepository extends JpaRepository<Guild, Long> {
    Optional<Guild> findByOwnerId(Long id);
    
}
