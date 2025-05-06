package com.mdev.chatcord.server.controller;

import com.mdev.chatcord.server.dto.JwtRequest;
import com.mdev.chatcord.server.dto.ProfileDTO;
import com.mdev.chatcord.server.model.ERoles;
import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import com.mdev.chatcord.server.service.EmailService;
import com.mdev.chatcord.server.service.JwtService;
import com.mdev.chatcord.server.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity
@RequestMapping("/api/auth/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/users/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUserDto(@PathVariable String uuid, @AuthenticationPrincipal Jwt jwt){

        if (!uuid.equals(jwt.getClaimAsString("uuid"))) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("The UUID for the Authenticated Token does not match with the UUID provided.");

        User user = userRepository.findByUuid(UUID.fromString(uuid));

        ProfileDTO profileDTO = new ProfileDTO(user.getEmail(), user.getUsername(), user.getTag(),
                user.getStatus().name(), user.getUserSocket(), user.isEmailVerified()
        );

        return ResponseEntity.ok(profileDTO);
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody JwtRequest jwtRequest){
        if (userRepository.existsByEmail(jwtRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email Already Registered.");

        User user = new User(jwtRequest.getEmail(), jwtRequest.getPassword(), jwtRequest.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok("Admin Registered Successfully");
    }


}
