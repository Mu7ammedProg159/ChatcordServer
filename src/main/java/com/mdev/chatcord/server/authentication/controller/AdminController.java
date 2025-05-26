package com.mdev.chatcord.server.authentication.controller;

import com.mdev.chatcord.server.authentication.dto.AuthenticationRequest;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.authentication.service.AuthenticationService;
import com.mdev.chatcord.server.authentication.service.ERoles;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.user.service.UserService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity
@RequestMapping("/api/auth/admin")
public class AdminController {

    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    private final AuthenticationManager authenticationManager;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UUID>> getAllUsersUUID(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(userService.getAllUUID(page, size));
    }

    @GetMapping("/users/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserInfo(@PathVariable String uuid, @AuthenticationPrincipal Jwt jwt){

        if (!uuid.equals(jwt.getClaimAsString("uuid"))) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("The UUID for the Authenticated Token does not match with the UUID provided.");

        ProfileDetails profileDetailsDTO = userService.getUserProfile(uuid);

        return ResponseEntity.ok(profileDetailsDTO);
    }

    @PostMapping("/register")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody AuthenticationRequest authenticationRequest){
        @Email
        String email = authenticationRequest.getEmail();

        emailService.validateEmailOtp(email);
        authenticationService.registerUser(authenticationRequest.getEmail(), authenticationRequest.getPassword(), authenticationRequest.getUsername(), ERoles.ADMIN);

        return ResponseEntity.ok("User Registered Successfully, " +
                "Please Verify your Email Address to avoid losing your account.");
    }

}
