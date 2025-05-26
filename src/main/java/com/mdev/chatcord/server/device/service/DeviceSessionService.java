package com.mdev.chatcord.server.device.service;

import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.Account;

import java.util.List;

public interface DeviceSessionService {
    public boolean existsForUser(Account user, String deviceId);
//    public boolean existsForUser(String subject, String deviceId);

    public void saveSession(Account user, String deviceId, String deviceName, String os, String osVersion, String ip);
//    public void saveSession(String subject, String deviceId, String deviceName, String os, String osVersion, String ip);

    void saveSession(Account user, DeviceDto deviceDto);

    //    public List<DeviceSession> getDevicesForUser(User user);
    public List<DeviceSession> getDevicesForUser(String subject);

    public void removeDevice(String subject, String deviceId);
}
