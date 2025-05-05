package com.mdev.chatcord.server.controller;

import com.mdev.chatcord.server.dto.JwtRequest;
import com.mdev.chatcord.server.dto.ProfileDTO;
import com.mdev.chatcord.server.model.*;
import com.mdev.chatcord.server.repository.UserRepository;
import com.mdev.chatcord.server.service.EmailService;
import com.mdev.chatcord.server.service.JwtService;
import com.mdev.chatcord.server.service.OtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
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
    public ResponseEntity<String> login(@Valid @RequestBody JwtRequest jwtRequest) {

        @Email(message = "Enter a valid email address.")
        String email = jwtRequest.getEmail();

        @Null(message = "Email or password is invalid.")
        User user = userRepository.findByEmail(email);

        var auth = new UsernamePasswordAuthenticationToken(email, jwtRequest.getPassword(), mapRolesToAuthorities(user));

        SecurityContextHolder.getContext().setAuthentication(auth);

        authenticationManager.authenticate(auth);

        String token = jwtService.generateToken(auth, user);

        logger.info("User with this Email Address: [{}] Logged In Successfully. His UUID is: ", auth.getName());
        logger.info("User with this Email Address: [{}] Has these Authorities.", auth.getAuthorities());

        return ResponseEntity.ok(token);
    }

    private static Set<SimpleGrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getRoles().stream()
                .map(roles -> new SimpleGrantedAuthority(roles.name()))
                .collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = MailSendException.class)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody JwtRequest jwtRequest){
        @Email(message = "Email address does not exists.")
        String email = jwtRequest.getEmail();

        if (userRepository.existsByEmail(email))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email Already Registered.");

        User user = new User(email, jwtRequest.getPassword(), jwtRequest.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.USER);

        String otp = otpService.generateOtp(email);

        emailService.sendOtpEmail(email, otp);
        logger.info("OTP {} email sent to {}", otp, email);

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
    public ResponseEntity<?> registerAdmin(@RequestBody JwtRequest jwtRequest){
        if (userRepository.existsByEmail(jwtRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email Already Registered.");

        User user = new User(jwtRequest.getEmail(), jwtRequest.getPassword(), jwtRequest.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok("Admin Registered Successfully");
    }

    @PostMapping("/authenticate")
    public Authentication authenticate(Authentication authentication){
        logger.info("These are the ROLES that {{}} has: [{}].", authentication.getName(), authentication.getAuthorities());

        logger.info("The UUID for this Account is: {}", userRepository.findByUuid(UUID.fromString(jwtService.getUUIDFromJwt(authentication))).getUsername());
        return authentication;
    }

    @GetMapping("/admin/users/{uuid}")
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

    @GetMapping("/users/me")
    public ResponseEntity<ProfileDTO> getUserProfile(Authentication authentication){

        User user = userRepository.findByEmail(authentication.getName());

        ProfileDTO profileDTO = new ProfileDTO(user.getEmail(), user.getUsername(), user.getTag(),
                user.getStatus().name(), user.getUserSocket(), user.isEmailVerified()
        );

        return ResponseEntity.ok(profileDTO);
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

    public record EmailRequest(@Email(message = "Email address does not exists.") String email){}
}
