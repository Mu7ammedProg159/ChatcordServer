package com.mdev.chatcord.server.device.model;

import com.mdev.chatcord.server.user.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DeviceSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    private String deviceId;
    private String deviceName;
    private String os;
    private String osVersion;
    private String ip;
    private Instant createdAt;

    public DeviceSession(Account user, String deviceId, String deviceName, String os, String osVersion, String ip, Instant createdAt) {
        this.user = user;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.os = os;
        this.osVersion = osVersion;
        this.ip = ip;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }

}
