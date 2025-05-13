package com.mdev.chatcord.server.user.controller;

import com.mdev.chatcord.server.direct.model.PrivateChat;
import com.mdev.chatcord.server.direct.repository.PrivateChatRepository;
import com.mdev.chatcord.server.user.dto.OtpRequest;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.service.EmailService;
import com.mdev.chatcord.server.user.service.OtpService;
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
@RequestMapping("/api/auth")
public class EmailController {

    private final UserRepository userRepository;
    private final PrivateChatRepository privateChatRepository;
    private final EmailService emailService;
    private final OtpService otpService;

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody OtpRequest otpRequest){
        if(otpService.validateOtp(otpRequest.email(), otpRequest.otp())) {

            User user = userRepository.findByEmail(otpRequest.email());
            if (user.isEmailVerified()) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("The Email Address is already verified.");

            user.setEmailVerified(true);
            user.setAccountNonLocked(true);

            PrivateChat privateChat;

            userRepository.save(user);

            return ResponseEntity.ok("Email Verified Successfully");
        }
        else {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
    }
}
