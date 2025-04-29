package com.mdev.chatcord.server.controller;

import com.mdev.chatcord.server.dto.UserDTO;
import com.mdev.chatcord.server.model.ERoles;
import com.mdev.chatcord.server.model.LoginRequest;
import com.mdev.chatcord.server.model.LoginResponse;
import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import com.mdev.chatcord.server.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws UsernameNotFoundException {
        try {
            var auth = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            authenticationManager.authenticate(auth);

            User user = userRepository.findByEmail(loginRequest.getEmail());
            var token = jwtService.generateToken(user);

            logger.info("User with this Email Address: [{}] Logged In Successfully.", user.getEmail());

            return ResponseEntity.ok(new LoginResponse(token));
        } catch (InternalAuthenticationServiceException iE){
            throw new InternalAuthenticationServiceException("This is error regarding the INTERNAL AUTHENTICATION SERVICE " + iE.getMessage());

        } catch (UsernameNotFoundException e){
            throw new UsernameNotFoundException("Account with this Email Address not found.");
        }
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
        userRepository.save(user);

        return ResponseEntity.ok("User Registered Successfully");
    }

    @PostMapping("/authenticate")
    public String authenticate(Authentication authentication){
        return authentication.getDetails().toString();
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
}
