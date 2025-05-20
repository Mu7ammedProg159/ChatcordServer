package com.mdev.chatcord.server.device.service;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.device.repository.DeviceSessionRepository;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceSessionService implements DeviceSessionServiceImpl{

    private final DeviceSessionRepository deviceSessionRepository;
    private final UserRepository userRepository;

    @Override
    public boolean existsForUser(User user, String deviceId) {
        return deviceSessionRepository.existsByUserAndDeviceId(user, deviceId);
    }

    @Override
    public void saveSession(User user, String deviceId, String deviceName, String os, String osVersion, String ip) {
        if (!existsForUser(user, deviceId)){
            DeviceSession session = DeviceSession.builder()
                    .user(user)
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .os(os)
                    .osVersion(osVersion)
                    .ip(ip)
                    .build();
            deviceSessionRepository.save(session);
        }
    }

    @Override
    public List<DeviceSession> getDevicesForUser(String subject) {

        User user = userRepository.findByEmail(subject);

        return deviceSessionRepository.findAll()
                .stream()
                .filter(d -> d.getUser().equals(user))
                .toList();
    }

    @Override
    @Transactional
    public void removeDevice(String subject, String deviceId) {
        User user = userRepository.findByEmail(subject);
        deviceSessionRepository.deleteByUserAndDeviceId(user, deviceId);
    }
}
