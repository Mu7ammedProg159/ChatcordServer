package com.mdev.chatcord.server.user.controller;

import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.authentication.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

}
