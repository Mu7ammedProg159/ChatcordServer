package com.mdev.chatcord.server.authentication.controller;

import com.mdev.chatcord.server.authentication.dto.RefreshDto;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import com.mdev.chatcord.server.device.service.EPlatform;
import com.mdev.chatcord.server.device.service.RequestMetadataUtil;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.authentication.dto.JwtRequest;
import com.mdev.chatcord.server.authentication.service.AuthenticationService;
import com.mdev.chatcord.server.token.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableAsync
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final OtpService otpService;
//    private final JwtService jwtService;
    private final DeviceSessionService deviceSessionService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest jwtRequest, HttpServletRequest httpHeaders) {

        String ip = RequestMetadataUtil.extractIp(httpHeaders);
        String userAgent = RequestMetadataUtil.extractUserAgent(httpHeaders);

        return ResponseEntity.ok(authenticationService.login(jwtRequest.getEmail(), jwtRequest.getPassword(), jwtRequest.getDeviceDto(), userAgent));
    }

    @PostMapping("/refresh-key")
    public ResponseEntity<?> refreshToken(Authentication authentication, RefreshDto refreshDto){
        return ResponseEntity.ok(authenticationService.refreshAccessToken(authentication, refreshDto));
    }

    @PostMapping("/register")
    @Transactional(rollbackFor = MailSendException.class)
    public ResponseEntity<?> register(@Valid @RequestBody JwtRequest jwtRequest) {
        @Email
        String email = jwtRequest.getEmail();
        authenticationService.registerUser(jwtRequest.getEmail(), jwtRequest.getPassword(), jwtRequest.getUsername());

        emailService.validateEmailOtp(email);

        return ResponseEntity.ok("User Registered Successfully, " +
                "Please Verify your Email Address to avoid losing your account.");
    }

    @PostMapping("/authenticate")
    public Authentication authenticate(Authentication authentication) {
        log.info("These are the ROLES that {{}} has: [{}].", authentication.getName(), authentication.getAuthorities());
        log.info("The UUID for this Account is: {}", UUID.fromString(tokenService.getUUIDFromJwt(authentication)));

        return authentication;
    }

    @DeleteMapping("/delete/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            emailService.deleteAccount(email);
            return ResponseEntity.ok("User with this Email Address [" + email + "] has been deleted.");

        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("Account with this Email Address not found.");
        }
    }

    public record EmailRequest(@Email(message = "Email address does not exists.") String email){}
}
