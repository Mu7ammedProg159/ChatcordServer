package com.mdev.chatcord.server.device.service;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.user.model.User;

import java.util.List;
import java.util.UUID;

public interface DeviceSessionServiceImpl {
    public boolean existsForUser(User user, String deviceId);
//    public boolean existsForUser(String subject, String deviceId);

    public void saveSession(User user, String deviceId, String deviceName, String os, String osVersion, String ip);
//    public void saveSession(String subject, String deviceId, String deviceName, String os, String osVersion, String ip);

//    public List<DeviceSession> getDevicesForUser(User user);
    public List<DeviceSession> getDevicesForUser(String subject);

    public void removeDevice(String subject, String deviceId);
}
