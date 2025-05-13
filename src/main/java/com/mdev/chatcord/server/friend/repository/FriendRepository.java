package com.mdev.chatcord.server.friend.repository;

import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByOwnerId(Long owner_id);
}
