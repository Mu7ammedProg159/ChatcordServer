package com.mdev.chatcord.server.device.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeviceDto {
    private String deviceId;
    private String deviceName;
    private String os;
    private String osVersion;
    private String ip;
}
