package com.mdev.chatcord.server.repository;

import com.mdev.chatcord.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    //User findByUsername(String username);
    User findByEmail(String email);
    User findByUsernameAndTag(String username, String tag);
    boolean existsByTag(String tag);
    boolean existsByEmail(String email);
}
