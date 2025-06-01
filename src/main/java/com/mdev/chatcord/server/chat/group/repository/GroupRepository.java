package com.mdev.chatcord.server.chat.group.repository;

import com.mdev.chatcord.server.chat.group.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupChat, Long> {
//    Optional<GroupChat> findByOwnerId(Long id);
    
}
