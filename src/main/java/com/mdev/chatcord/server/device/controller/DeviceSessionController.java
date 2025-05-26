package com.mdev.chatcord.server.device.controller;

import com.mdev.chatcord.server.device.dto.DeviceVerificationDTO;
import com.mdev.chatcord.server.device.model.DeviceSession;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.token.service.TokenService;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/devices")
public class DeviceSessionController {

    private final DeviceSessionService deviceSessionService;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<DeviceSession>> getMyDevices(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(deviceSessionService.getDevicesForUser(jwt.getSubject()));
    }

    @PostMapping("/device/verify")
    public ResponseEntity<?> verifyDevice(@RequestBody DeviceVerificationDTO deviceDTO){
        if(otpService.validateOtp(deviceDTO.getEmail(), deviceDTO.getOtp())) {

            Account account = accountRepository.findByEmail(deviceDTO.getEmail());

            account.setEmailVerified(true);
            account.setAccountNonLocked(true);

            accountRepository.save(account);

            deviceSessionService.saveSession(account, deviceDTO.getDeviceDto());

            return ResponseEntity.ok("Device Verified Successfully");
        }
        else {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
    }

    @DeleteMapping("/device/{deviceId}")
    public ResponseEntity<?> removeDevice(@AuthenticationPrincipal Jwt jwt, @PathVariable String deviceId){
        deviceSessionService.removeDevice(jwt.getSubject(), deviceId);
        log.info("THE EMAIL THAT REQUESTED THE DELETION IS: {}", jwt.getSubject());
        return ResponseEntity.ok("Device disconnected successfully");
    }

}
