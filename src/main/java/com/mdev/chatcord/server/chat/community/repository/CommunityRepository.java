package com.mdev.chatcord.server.chat.community.repository;

import com.mdev.chatcord.server.chat.community.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByOwnerId(Long id);
    
}
