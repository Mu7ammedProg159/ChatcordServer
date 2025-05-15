package com.mdev.chatcord.server.user.repository;

import com.mdev.chatcord.server.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByUsernameAndTag(String username, String tag);

    boolean existsByTag(String tag);
    boolean existsByUsernameAndTag(String username, String tag);
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM User u")
    Page<UUID> findAllByUuid(Pageable pageable);

    void deleteByEmail(String email);
}
