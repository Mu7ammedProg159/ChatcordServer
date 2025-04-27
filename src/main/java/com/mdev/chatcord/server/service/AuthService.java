package com.mdev.chatcord.server.service;

import com.mdev.chatcord.server.model.EStatus;
import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private ApplicationEventPublisher eventPublisher;

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    public String signIn(String email, String key){
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(key, user.getPassword())){
            user.setStatus(EStatus.Online);
            logger.info("Username [{}] logged in successfully.", user.getUsername());
            return jwtService.generateToken(user.getUsername(), user.getTag());
        }
        else
            logger.error("Email Address Or Password are incorrect.");
        return null;
    }

    public String generateTag(){
        Random random = new Random();
        int id = random.nextInt(9000) + 1000;
        while (userRepository.existsByTag(String.valueOf(id))){
            id = random.nextInt(9000) + 1000;
        }

        return String.valueOf(id);
    }

    public boolean isUserRegistered(String email){
        return userRepository.findByEmail(email) != null;
    }

    public void logoutUser(String username, String tag){
        User user = userRepository.findByUsernameAndTag(username, tag);
        user.setStatus(EStatus.Offline);
    }

    public void signUp(String email, String username, String key){
        try{
            User user = new User();

            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(key));
            user.setTag(generateTag());
            user.setStatus(EStatus.Offline);
            userRepository.save(user);
        } catch (Exception e){
            System.out.println("User already registered with this username.");
        }
    }
}
