package com.mdev.chatcord.server.device.repository;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {

    Optional<DeviceSession> findByProfileAndDeviceId(Profile profile, String deviceId);
    Optional<DeviceSession> findByProfileId(Long id);

    boolean existsByProfileAndDeviceId(Profile profile, String deviceId);

    @Transactional
    void deleteByProfileAndDeviceId(Profile profile, String deviceId);
}
