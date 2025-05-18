package com.mdev.chatcord.server.device.repository;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {

    Optional<DeviceSession> findByUserIdAndDeviceId(User user, String deviceId);

    boolean existsByUserIdAndDeviceId(User user, String deviceId);

    void deleteByUserIdAndDeviceId(User user, String deviceId);
}
