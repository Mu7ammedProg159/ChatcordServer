package com.mdev.chatcord.server.authentication.service;

import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.exception.AlreadyRegisteredException;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import com.mdev.chatcord.server.user.service.EUserState;
import com.mdev.chatcord.server.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final EmailService emailService;
    private final UserService userService;

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final ProfileRepository profileRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;


    public String login(@Valid @Email(message = "Enter a valid email address.") String email, String password){

        if (!emailService.isEmailRegistered(email))
            throw new UsernameNotFoundException("Account with this Email Address not found.");

        if (!emailService.isEmailVerified(email)){
            if (otpService.canResendOtp(email) <= 0)
                otpService.generateOtp(email);
            throw new LockedException("Please verify your email.");
        }

        @Null(message = "Email or password is invalid.")
        User user = userRepository.findByEmail(email);

        var auth = new UsernamePasswordAuthenticationToken(email, password, mapRolesToAuthorities(user));

        SecurityContextHolder.getContext().setAuthentication(auth);

        authenticationManager.authenticate(auth);

        String token = jwtService.generateToken(auth, user);

        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElseThrow();
        userStatus.setStatus(EUserState.ONLINE);

        userStatusRepository.save(userStatus);

        log.info("User with this Email Address: [{}] Logged In Successfully. His UUID is: ", auth.getName());
        log.info("User with this Email Address: [{}] Has these Authorities.", auth.getAuthorities());

        return token;
    }

    public void registerUser(@Valid @Email(message = "Enter a valid email address.") String email, String password, String username){

        if (emailService.isEmailRegistered(email))
            throw new AlreadyRegisteredException("Account with this Email Address already registered.");


        @Null(message = "BAD REQUEST: Something went wrong when adding new friend.")
        User user = new User(email, password, username);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.USER);

        userService.createUser(user);

    }

    public void registerUser(@Valid @Email(message = "Enter a valid email address.") String email, String password, String username, ERoles role){

        @Null(message = "BAD REQUEST: Something went wrong when adding new friend.")
        User user = new User(email, password, username);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(role);

        userService.createUser(user);

    }

    private Set<SimpleGrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getRoles().stream()
                .map(roles -> new SimpleGrantedAuthority(roles.name()))
                .collect(Collectors.toSet());
    }
}
