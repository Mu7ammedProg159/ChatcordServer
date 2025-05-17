package com.mdev.chatcord.server.device.repository;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {

    Optional<DeviceSession> findByUserIdAndDeviceId(User user, String deviceId);

    boolean existsByUserIdAndDeviceId(User user, String deviceId);

    void deleteByUserIdAndDeviceId(User user, String deviceId);
}
