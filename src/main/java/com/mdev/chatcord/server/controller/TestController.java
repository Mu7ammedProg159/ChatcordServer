package com.mdev.chatcord.server.controller;

import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableMethodSecurity
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String hello() {
        return "Web is working!";
    }


    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers() {
        return "Web is working!";
    }
}