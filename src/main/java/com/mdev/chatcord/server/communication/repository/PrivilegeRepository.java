package com.mdev.chatcord.server.communication.repository;

import com.mdev.chatcord.server.communication.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

}