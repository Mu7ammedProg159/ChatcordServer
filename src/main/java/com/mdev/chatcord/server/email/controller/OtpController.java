package com.mdev.chatcord.server.email.controller;


import com.mdev.chatcord.server.authentication.controller.AuthenticationController;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@EnableMethodSecurity
@RequestMapping("api/auth/otp")
public class OtpController {

    private final EmailService emailService;
    private final OtpService otpService;

    @PostMapping("/resend")
    public ResponseEntity<String> resendOtp(@RequestBody AuthenticationController.EmailRequest emailRequest) {
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
    public ResponseEntity<?> canRetrySend(@RequestBody AuthenticationController.EmailRequest emailRequest) {
        if (!emailService.isEmailRegistered(emailRequest.email()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not Registered.");

        if (emailService.isEmailVerified(emailRequest.email())) return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS).body("This Email Address is already verified.");

        long remaining = otpService.canResendOtp(emailRequest.email());

        return ResponseEntity.ok(remaining);
    }

}
