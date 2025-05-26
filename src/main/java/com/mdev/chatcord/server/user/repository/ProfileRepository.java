package com.mdev.chatcord.server.user.repository;

import com.mdev.chatcord.server.user.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByAccountId(Long id);
    Optional<Profile> findByAccountEmail(String email);

    @Query("SELECT u.id FROM Profile u")
    Page<UUID> findAllByUuid(Pageable pageable);

    boolean existsByTag(String tag);
    boolean existsByUsernameAndTag(String username, String tag);

    Optional<Profile> findByUuid(UUID uuid);
    Optional<Profile> findByUsernameAndTag(String username, String tag);

}
