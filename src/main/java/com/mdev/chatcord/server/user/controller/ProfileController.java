package com.mdev.chatcord.server.user.controller;

import com.mdev.chatcord.server.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final AccountRepository accountRepository;
    //private final JwtService jwtService;

}
