package com.mdev.chatcord.server.user.repository;

import com.mdev.chatcord.server.user.model.UserStatus;
import jakarta.validation.constraints.Null;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findByUserId(Long id);

    @Null(message = "INTERNAL SERVER ERROR: There is no profile in this account.")
    Optional<UserStatus> findByProfileId(Long id);
}
