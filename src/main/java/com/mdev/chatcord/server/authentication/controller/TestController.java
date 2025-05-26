package com.mdev.chatcord.server.authentication.controller;

import com.mdev.chatcord.server.token.service.TokenService;
import com.mdev.chatcord.server.user.repository.AccountRepository;
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

    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return tokenService.getUUIDFromJwt(authentication);
    }


    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers() {
        return "Web is working!";
    }
}