package com.mdev.chatcord.server.chat.group.repository;

import com.mdev.chatcord.server.chat.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByOwnerId(Long id);
    
}
