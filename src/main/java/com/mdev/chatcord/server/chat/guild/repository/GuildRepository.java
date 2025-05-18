package com.mdev.chatcord.server.chat.guild.repository;

import com.mdev.chatcord.server.chat.guild.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
    Optional<Guild> findByOwnerId(Long id);
    
}
