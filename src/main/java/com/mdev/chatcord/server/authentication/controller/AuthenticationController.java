package com.mdev.chatcord.server.authentication.controller;

import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.exception.AlreadyRegisteredException;
import com.mdev.chatcord.server.authentication.dto.JwtRequest;
import com.mdev.chatcord.server.authentication.service.AuthenticationService;
import com.mdev.chatcord.server.authentication.service.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.LockedException;
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
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody JwtRequest jwtRequest) {

        String token = authenticationService.login(jwtRequest.getEmail(), jwtRequest.getPassword());
        return ResponseEntity.ok(token);
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

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody EmailRequest emailRequest) {
        if (!emailService.isEmailRegistered(emailRequest.email()))
            return ResponseEntity.badRequest().body("Email not Registered.");

        long remaining = otpService.canResendOtp(emailRequest.email());
        if (remaining > 0) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Please wait at least " + remaining + " seconds before resending the OTP.");

        if (emailService.isEmailVerified(emailRequest.email())) return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS).body("This Email Address is already verified.");

        String newOtp = otpService.generateOtp(emailRequest.email());
        emailService.sendOtpEmail(emailRequest.email(), newOtp);

        return ResponseEntity.ok("OTP resent successfully.");
    }

    @PostMapping("/retry-otp-send")
    public ResponseEntity<?> canRetrySend(@RequestBody EmailRequest emailRequest) {
        if (!emailService.isEmailRegistered(emailRequest.email()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not Registered.");

        if (emailService.isEmailVerified(emailRequest.email())) return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS).body("This Email Address is already verified.");

        long remaining = otpService.canResendOtp(emailRequest.email());

        return ResponseEntity.ok(remaining);
    }

    @PostMapping("/authenticate")
    public Authentication authenticate(Authentication authentication) {
        log.info("These are the ROLES that {{}} has: [{}].", authentication.getName(), authentication.getAuthorities());
        log.info("The UUID for this Account is: {}", UUID.fromString(jwtService.getUUIDFromJwt(authentication)));

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
