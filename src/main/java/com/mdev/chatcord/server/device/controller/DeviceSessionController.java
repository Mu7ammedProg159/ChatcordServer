package com.mdev.chatcord.server.device.controller;

import com.mdev.chatcord.server.authentication.dto.JwtRequest;
import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final OtpService otpService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<DeviceSession>> getMyDevices(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(deviceSessionService.getDevicesForUser(jwt.getSubject()));
    }

    @PostMapping("/device/verify")
    public ResponseEntity<?> verifyDevice(JwtRequest jwtRequest, String otp){
        if(otpService.validateOtp(jwtRequest.getEmail(), otp)) {

            User user = userRepository.findByEmail(jwtRequest.getEmail());

            user.setEmailVerified(true);
            user.setAccountNonLocked(true);

            userRepository.save(user);

            return ResponseEntity.ok("Email Verified Successfully");
        }
        else {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
    }

    @DeleteMapping("/device/{deviceId}")
    public ResponseEntity<?> removeDevice(@AuthenticationPrincipal Jwt jwt, @PathVariable String deviceId){
        deviceSessionService.removeDevice(jwt.getSubject(), deviceId);
        return ResponseEntity.ok("Device disconnected successfully");
    }

}
