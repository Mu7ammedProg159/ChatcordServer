package com.mdev.chatcord.server.device.repository;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {

    Optional<DeviceSession> findByUserAndDeviceId(User user, String deviceId);

    boolean existsByUserAndDeviceId(User user, String deviceId);

    void deleteByUserAndDeviceId(User user, String deviceId);
}
