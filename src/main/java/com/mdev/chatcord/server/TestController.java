package com.mdev.chatcord.server;

import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.authentication.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableMethodSecurity
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return jwtService.getUUIDFromJwt(authentication);
    }


    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers() {
        return "Web is working!";
    }
}