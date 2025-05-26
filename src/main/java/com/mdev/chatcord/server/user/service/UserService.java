package com.mdev.chatcord.server.user.service;

import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@EnableAsync
@Slf4j
public class UserService {

    private final AccountRepository accountRepository;
    private final UserStatusRepository userStatusRepository;
    private final ProfileRepository profileRepository;

    private final AuthenticationManager authenticationManager;

    private final String default_pfp = "/images/default_pfp.png";

    @Transactional(rollbackFor = Exception.class)
    public void createUser(Account account, String username){

        Profile profile = new Profile(username, null, default_pfp, null);

        profile.setUserStatus(new UserStatus(EUserState.OFFLINE));
        account.setProfile(profile);

        accountRepository.save(account);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProfileDetails getUserProfile(String uuid){

        Profile profile = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        return new ProfileDetails(profile.getUuid(), profile.getUsername(), profile.getTag(),
                profile.getUserStatus().getStatus().name(), profile.getAvatarUrl(),
                profile.getAboutMe(), profile.getQuote());
    }

    @Transactional(rollbackFor = Exception.class)
    public Page<UUID> getAllUUID(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return profileRepository.findAllByUuid(pageable);
    }

}
