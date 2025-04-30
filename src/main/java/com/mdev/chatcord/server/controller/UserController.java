package com.mdev.chatcord.server.controller;

import com.mdev.chatcord.server.dto.LoginRequest;
import com.mdev.chatcord.server.dto.LoginResponse;
import com.mdev.chatcord.server.model.*;
import com.mdev.chatcord.server.repository.UserRepository;
import com.mdev.chatcord.server.service.EmailService;
import com.mdev.chatcord.server.service.JwtService;
import com.mdev.chatcord.server.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpService otpService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userRepository.findByEmail(loginRequest.getEmail());
            var auth = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword(), mapRolesToAuthorities(user));

            authenticationManager.authenticate(auth);

            var token = jwtService.generateToken(auth);

            logger.info("User with this Email Address: [{}] Logged In Successfully.", auth.getName());
            logger.info("User with this Email Address: [{}] Has these Authorities.", auth.getAuthorities());

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (UsernameNotFoundException usernameNotFoundException){
            return ResponseEntity.badRequest().body("This Email Address is not registered.");
        } catch (BadCredentialsException e){
            return ResponseEntity.badRequest().body("Email Address or Password are incorrect.");
        } catch (LockedException accountLockedException){
            return ResponseEntity.status(HttpStatus.LOCKED).body("Please verify your Email Address first before logging in.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something Went Wrong.");
        }

    }

    private static Set<SimpleGrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getRoles().stream()
                .map(roles -> new SimpleGrantedAuthority(roles.name()))
                .collect(Collectors.toSet());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest loginRequest){
        if (userRepository.existsByEmail(loginRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email Already Registered.");

        String tag = jwtService.generateTag(userRepository);
        User user = new User(loginRequest.getEmail(), loginRequest.getPassword(), loginRequest.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.USER);
        user.setTag(tag);

        String otp = otpService.generateOtp(loginRequest.getEmail());

        try{
            emailService.sendOtpEmail(loginRequest.getEmail(), otp);
            logger.info("OTP {} email sent to {}", otp, loginRequest.getEmail());

        } catch (Exception e){
            throw new RuntimeException(e);
        }

        userRepository.save(user);
        return ResponseEntity.ok("User Registered Successfully, " +
                "Please Verify your Email Address to avoid losing your account.");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody EmailRequest emailRequest){
        if (!userRepository.existsByEmail(emailRequest.email())) return ResponseEntity.badRequest().body("Email not Registered.");

        if (!otpService.canResendOtp(emailRequest.email())) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Please wait at least 1 minute before resending the OTP.");

        if (userRepository.findByEmail(emailRequest.email()).isEmailVerified()) return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS).body("This Email Address is already verified.");

        String newOtp = otpService.generateOtp(emailRequest.email());
        emailService.sendOtpEmail(emailRequest.email(), newOtp);

        return ResponseEntity.ok("OTP resent successfully.");

    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody LoginRequest loginRequest){
        if (userRepository.existsByEmail(loginRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email Already Registered.");

        String tag = jwtService.generateTag(userRepository);
        User user = new User(loginRequest.getEmail(), loginRequest.getPassword(), loginRequest.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.ADMIN);
        user.setTag(tag);
        userRepository.save(user);

        return ResponseEntity.ok("Admin Registered Successfully");
    }

    @PostMapping("/authenticate")
    public Authentication authenticate(Authentication authentication){
        logger.info("These are the ROLES that {{}} has: [{}].", authentication.getName(), authentication.getAuthorities());
        return authentication;
    }

    @DeleteMapping("/delete/user")
    public ResponseEntity<?> deleteUser(Authentication authentication){
        try{
            String email = authentication.getName();
            userRepository.deleteByEmail(email);
            return ResponseEntity.ok("User with this Email Address [" + email + "] has been deleted.");

        } catch (UsernameNotFoundException e){
            throw new UsernameNotFoundException("Account with this Email Address not found.");
        }
    }

    public record EmailRequest(String email){}
}
