package com.mdev.chatcord.server.device.model;

import com.mdev.chatcord.server.BaseEntity;
import com.mdev.chatcord.server.user.model.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DeviceSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Profile profile;

    private String deviceId;
    private String deviceName;
    private String os;
    private String osVersion;
    private String ip;

    public DeviceSession(String deviceId, String deviceName, String os, String osVersion, String ip) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.os = os;
        this.osVersion = osVersion;
        this.ip = ip;
    }

}
