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
    private String DEVICE_ID;
    private String DEVICE_NAME;
    private String OS;
    private String OS_VERSION;
    private String LOCAL_IP_ADDRESS;
}
