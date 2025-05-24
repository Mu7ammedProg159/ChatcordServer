package com.mdev.chatcord.server.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeviceVerificationDTO {
    private String email;
    @JsonProperty(namespace = "DeviceInfo")
    private DeviceDto deviceDto;
    private String otp;
}
