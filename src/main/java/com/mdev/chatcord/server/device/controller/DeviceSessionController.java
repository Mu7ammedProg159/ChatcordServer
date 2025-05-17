package com.mdev.chatcord.server.device.controller;

import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequiredArgsConstructor
@RequestMapping("api/devices")
public class DeviceSessionController {

    private final DeviceSessionService deviceSessionService;

    @GetMapping
    public ResponseEntity<List<DeviceSession>> getMyDevices(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(deviceSessionService.getDevicesForUser(jwt.getSubject()));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> removeDevice(@AuthenticationPrincipal Jwt jwt, @PathVariable String deviceId){
        deviceSessionService.removeDevice(jwt.getSubject(), deviceId);
        return ResponseEntity.ok("Device disconnected successfully");
    }

}
