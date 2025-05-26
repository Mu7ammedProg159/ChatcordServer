package com.mdev.chatcord.server.user.repository;

import com.mdev.chatcord.server.authentication.service.ERoles;
import com.mdev.chatcord.server.user.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);

    @Query("SELECT a.roles FROM Account a WHERE a.email = :email")
    Set<ERoles> findRolesByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);
}
