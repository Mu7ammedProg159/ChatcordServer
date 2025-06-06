package com.mdev.chatcord.server.device.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeviceDto {
    @JsonProperty("DEVICE_ID")
    private String DEVICE_ID;
    @JsonProperty("DEVICE_NAME")
    private String DEVICE_NAME;
    @JsonProperty("OS")
    private String OS;
    @JsonProperty("OS_VERSION")
    private String OS_VERSION;
}
