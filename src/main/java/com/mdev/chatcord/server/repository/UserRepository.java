package com.mdev.chatcord.server.repository;

import com.mdev.chatcord.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    //User findByUsername(String username);
    User findByEmail(String email);
    User findByUuid(UUID uuid);
    User findByUsernameAndTag(String username, String tag);
    boolean existsByTag(String tag);
    boolean existsByUsernameAndTag(String username, String tag);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
