package com.mdev.chatcord.server.user.service;

import com.mdev.chatcord.server.user.dto.JwtRequest;
import com.mdev.chatcord.server.user.dto.Profile;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final ProfileRepository profileRepository;

    private final String default_pfp = "/images/default_pfp.png";

    public void createUser(String email, String password, String username){
        User user = new User(email, password, username);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(ERoles.USER);

        UserStatus userStatus = new UserStatus(user, user.getStatus());
        UserProfile userProfile = new UserProfile(user, null, default_pfp, null);

        userRepository.save(user);
        userStatusRepository.save(userStatus);
        profileRepository.save(userProfile);

    }


}
