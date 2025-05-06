package com.mdev.chatcord.server.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(email, new OtpEntry(otp, Instant.now().plus(Duration.ofMinutes(10)), LocalDateTime.now()));
        return otp;
    }

    public boolean validateOtp(String email, String submittedOtp) {
        OtpEntry entry = otpStorage.get(email);
        if (entry == null || Instant.now().isAfter(entry.expiry)) return false;
        return entry.otp.equals(submittedOtp);
    }

    public long canResendOtp(String email){
        OtpEntry otpEntry = otpStorage.get(email);
        if (otpEntry == null) return 0; // true means there are no any OTPs sent before.

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(otpEntry.lastSentAt(), now);
        long remaining = 60 - duration.toSeconds();

        return Math.max(remaining, 0);

    }

    public record OtpEntry(String otp, Instant expiry, LocalDateTime lastSentAt){}
}