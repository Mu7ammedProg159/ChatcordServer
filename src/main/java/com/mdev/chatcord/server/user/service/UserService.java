package com.mdev.chatcord.server.user.service;

import com.mdev.chatcord.server.user.dto.Profile;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@EnableAsync
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final ProfileRepository profileRepository;

    private final AuthenticationManager authenticationManager;

    private final String default_pfp = "/images/default_pfp.png";

    public void createUser(User user){

        @Null(message = "INTERNAL SERVER ERROR: There is no user status in this account.")
        UserStatus userStatus = new UserStatus(user, EUserState.OFFLINE);

        @Null(message = "INTERNAL SERVER ERROR: There is no profile in this account.")
        UserProfile userProfile = new UserProfile(user, null, default_pfp, null);

        userRepository.save(user);
        userStatusRepository.save(userStatus);
        profileRepository.save(userProfile);
    }

    public Profile getUserProfile(@Valid @Email(message = "Enter a valid email address.") String email){

        @Null(message = "Account with this email address does not exists.")
        User user = userRepository.findByEmail(email);

        @Null(message = "INTERNAL SERVER ERROR: There is no user status in this account.")
        Optional<UserProfile> userProfile = profileRepository.findByUserId(user.getId());

        @Null(message = "INTERNAL SERVER ERROR: There is no profile in this account.")
        Optional<UserStatus> userStatus = userStatusRepository.findByUserId(user.getId());

        return new Profile(user.getEmail(), user.getUsername(), user.getTag(),
                userStatus.get().getStatus().name(), userProfile.get().getProfilePictureUrl(),
                userProfile.get().getAboutMe(), userProfile.get().getQuote());

    }

    public Profile getUserProfileByUUID(@Valid String uuid){

        @Null(message = "Account with this UUID does not exists.")
        User user = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new UsernameNotFoundException(""));

        @Null(message = "INTERNAL SERVER ERROR: There is no user status in this account.")
        Optional<UserProfile> userProfile = profileRepository.findByUserId(user.getId());

        @Null(message = "INTERNAL SERVER ERROR: There is no profile in this account.")
        Optional<UserStatus> userStatus = userStatusRepository.findByUserId(user.getId());

        return new Profile(user.getEmail(), user.getUsername(), user.getTag(),
                userStatus.get().getStatus().name(), userProfile.get().getProfilePictureUrl(),
                userProfile.get().getAboutMe(), userProfile.get().getQuote());

    }

    public Page<UUID> getAllUUID(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllByUuid(pageable);
    }

}
