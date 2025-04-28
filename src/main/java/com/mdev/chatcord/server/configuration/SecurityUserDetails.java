package com.mdev.chatcord.server.configuration;

import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;

//@Configuration
public class SecurityUserDetails implements UserDetails {

    private UserRepository userRepository;


    public SecurityUserDetails(UserRepository userRepository){
        this.userRepository = userRepository;

    }

    @Bean
    public UserDetails loadUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(String.valueOf(user.getRole())) // <-- automatically adds "ROLE_" prefix
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
