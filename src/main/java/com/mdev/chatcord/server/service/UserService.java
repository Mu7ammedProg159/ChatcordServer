package com.mdev.chatcord.server.service;

import com.mdev.chatcord.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByEmail(String email) {
        var user = userRepository.findByEmail(email);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(String.valueOf(user.getRole())) // <-- automatically adds "ROLE_" prefix
                .build();
    }
}
