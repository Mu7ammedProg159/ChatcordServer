package com.mdev.chatcord.server.user.repository;

import com.mdev.chatcord.server.user.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);
    Optional<Account> findByUuid(UUID uuid);
    Optional<Account> findByUsernameAndTag(String username, String tag);

    boolean existsByTag(String tag);
    boolean existsByUsernameAndTag(String username, String tag);
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM Account u")
    Page<UUID> findAllByUuid(Pageable pageable);

    void deleteByEmail(String email);
}
