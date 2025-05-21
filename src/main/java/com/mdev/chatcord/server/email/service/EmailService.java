package com.mdev.chatcord.server.email.service;

import com.mdev.chatcord.server.device.service.IpLocationService;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final IpLocationService locationService;

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
            throw new BusinessException(ExceptionCode.INVALID_EMAIL);
        }
    }

    @Async
    public void sendOtpEmailWithBody(String toEmail, String body, String otp){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("dev.chatcord@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Chatcord");
            message.setText(body + otp);
            mailSender.send(message);
        } catch (MailSendException e){
            throw new BusinessException(ExceptionCode.INVALID_EMAIL);
        }
    }

    @Async
    public void validateEmailOtp(String email){
        if (isEmailVerified(email))
            throw new BusinessException(ExceptionCode.EMAIL_ALREADY_VERIFIED);

        String otp = otpService.generateOtp(email);

        sendOtpEmail(email, otp);
        log.info("OTP {} email sent to {}", otp, email);
    }

    @Async
    public void validateNewDevice(String email, String os, String deviceName, String ip){
        if (!isEmailVerified(email))
            throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED);

        String otp = otpService.generateOtp(email);
        var location = locationService.getLocation(ip);

        String newDeviceBody = "New Device Detected ! \n Device: " + os +  " \n DeviceName: " + deviceName + " \n COUNTRY: "
                + location.getCountry() + " \n CITY: " + location.getCity() + " \n\n One-Time New Device Verification code is: ";

        sendOtpEmailWithBody(email, newDeviceBody, otp);

        log.info("New Device Detected ! \n Device: {} \n DeviceName: {} \n COUNTRY: {} \n CITY: {} \n\n One-Time New Device Verification code is: ",
                os, deviceName, location.getCountry(), location.getCity());

        log.info("One-Time new Device code: {} sent to {}", otp, email);
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
