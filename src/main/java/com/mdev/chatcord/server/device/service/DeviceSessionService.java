package com.mdev.chatcord.server.device.service;

import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.Profile;

import java.util.List;

public interface DeviceSessionService {
    boolean existsForUser(Profile profile, String deviceId);
//    public boolean existsForUser(String subject, String deviceId);

    void saveSession(Profile profile, String deviceId, String deviceName, String os, String osVersion, String ip);
//    public void saveSession(String subject, String deviceId, String deviceName, String os, String osVersion, String ip);

    void saveSession(String email, DeviceDto deviceDto);

    //    public List<DeviceSession> getDevicesForUser(User user);
    public List<DeviceSession> getDevicesForUser(String subject);

    public void removeDevice(String subject, String deviceId);
}
