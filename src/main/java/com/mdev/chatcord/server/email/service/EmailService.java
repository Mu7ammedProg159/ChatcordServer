package com.mdev.chatcord.server.email.service;

import com.mdev.chatcord.server.exception.AlreadyRegisteredException;
import com.mdev.chatcord.server.exception.AlreadyVerifiedException;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final UserRepository userRepository;
    private final OtpService otpService;

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("dev.chatcord@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Chatcord");
            message.setText("Your verification code is: " + otp);
            mailSender.send(message);
        } catch (MailSendException e){
            throw new RuntimeException("Invalid email address.");
        }
    }

    @Async
    public void validateEmailOtp(String email){
        if (isEmailVerified(email))
            throw new AlreadyVerifiedException("Email address already verified.");

        String otp = otpService.generateOtp(email);

        sendOtpEmail(email, otp);
        log.info("OTP {} email sent to {}", otp, email);
    }

    public boolean isEmailRegistered(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean isEmailVerified(String email){
        return userRepository.findByEmail(email).isEmailVerified();
    }

    public void deleteAccount(String email){
        userRepository.deleteByEmail(email);
    }
}
