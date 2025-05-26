package com.mdev.chatcord.server.device.service;

import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.device.repository.DeviceSessionRepository;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceSessionServiceImpl implements DeviceSessionService{

    private final DeviceSessionRepository deviceSessionRepository;
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    @Override
    public boolean existsForUser(Profile profile, String deviceId) {
        return deviceSessionRepository.existsByProfileAndDeviceId(profile, deviceId);
    }

    @Override
    public void saveSession(Profile profile, String deviceId, String deviceName, String os, String osVersion, String ip) {
        if (!existsForUser(profile, deviceId)){
            DeviceSession session = DeviceSession.builder()
                    .profile(profile)
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
    public void saveSession(String email, DeviceDto deviceDto) {
        Profile profile = profileRepository.findByAccountEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.INVALID_EMAIL));

        if (!existsForUser(profile, deviceDto.getDEVICE_ID())){
            DeviceSession deviceSession = DeviceSession.builder()
                    .profile(profile)
                    .deviceId(deviceDto.getDEVICE_ID())
                    .deviceName(deviceDto.getDEVICE_NAME())
                    .os(deviceDto.getOS())
                    .osVersion(deviceDto.getOS_VERSION())
                    .build();

            deviceSessionRepository.save(deviceSession);
        }
    }

    @Override
    public List<DeviceSession> getDevicesForUser(String subject) {

        Profile profile = profileRepository.findByAccountEmail(subject)
                .orElseThrow(() -> new BusinessException(ExceptionCode.INVALID_EMAIL));

        return deviceSessionRepository.findAll()
                .stream()
                .filter(d -> d.getProfile().equals(profile))
                .toList();
    }

    @Override
    @Transactional
    public void removeDevice(String subject, String deviceId) {
        Profile profile = profileRepository.findByAccountEmail(subject)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        deviceSessionRepository.deleteByProfileAndDeviceId(profile, deviceId);
    }
}
