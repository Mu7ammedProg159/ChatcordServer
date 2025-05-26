package com.mdev.chatcord.server.email.controller;

import com.mdev.chatcord.server.email.dto.OtpRequest;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity
@RequestMapping("/api/auth/email")
public class EmailController {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final OtpService otpService;

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody OtpRequest otpRequest){
        if(otpService.validateOtp(otpRequest.email(), otpRequest.otp())) {

            Account account = accountRepository.findByEmail(otpRequest.email());
            if (account.isEmailVerified()) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("The Email Address is already verified.");

            account.setEmailVerified(true);
            account.setAccountNonLocked(true);

            accountRepository.save(account);

            return ResponseEntity.ok("Email Verified Successfully");
        }
        else {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
    }
}
