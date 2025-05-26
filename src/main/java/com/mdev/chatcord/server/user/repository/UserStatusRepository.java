package com.mdev.chatcord.server.user.repository;

import com.mdev.chatcord.server.user.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findById(Long id);
    Optional<UserStatus> findByUserId(Long id);
}
