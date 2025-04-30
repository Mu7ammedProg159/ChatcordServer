package com.mdev.chatcord.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dev.chatcord@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Chatcord Verification Code");
        message.setText("You verification code is: " + otp);
        mailSender.send(message);
    }
}
