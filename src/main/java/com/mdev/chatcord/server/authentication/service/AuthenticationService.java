package com.mdev.chatcord.server.authentication.service;

import com.mdev.chatcord.server.authentication.dto.AuthenticationResponse;
import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import com.mdev.chatcord.server.device.service.IpLocationService;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.exception.*;
import com.mdev.chatcord.server.redis.service.RefreshTokenStore;
import com.mdev.chatcord.server.token.service.TokenService;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import com.mdev.chatcord.server.user.service.EUserState;
import com.mdev.chatcord.server.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class  AuthenticationService {

    private final EmailService emailService;
    private final UserService userService;

    private final AccountRepository accountRepository;
    private final UserStatusRepository userStatusRepository;
    private final ProfileRepository profileRepository;

    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    private final DeviceSessionService deviceSessionService;
    private final TokenService tokenService;
    private final RefreshTokenStore refreshTokenStore;
    private final IpLocationService locationService;

    @Transactional(rollbackFor = Exception.class)
    public AuthenticationResponse login(@Valid @Email(message = "Enter a valid email address.") String email,
                                        String password, DeviceDto deviceDto, String userAgent,  String IP_ADDRESS){

        if (!emailService.isEmailRegistered(email))
            throw new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND);

        Profile profile = profileRepository.findByAccountEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Set<ERoles> rolesByEmail = accountRepository.findRolesByEmail(email);

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password, mapRolesToAuthorities(rolesByEmail))
            );

        } catch (BadCredentialsException ex){
            throw new BusinessException(ExceptionCode.INVALID_CREDENTIALS);
        } catch (LockedException e) {
            if (otpService.canResendOtp(email) <= 0)
                emailService.validateEmailOtp(email);
            throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED);
        }

        String refreshToken = null;

        if (userAgent != null && userAgent.equalsIgnoreCase("ReactorNetty/1.2.4")){
            deviceDto.setOS(userAgent);
            deviceDto.setDEVICE_NAME("Web-Browser");
        }

        var location = locationService.getLocation(IP_ADDRESS);
        if (!deviceSessionService.existsForUser(profile, deviceDto.getDEVICE_ID())) {
            log.info("Account with UUID: {} tried to login from: [DeviceId: {}, DeviceName: {}, OS: {}, Version: {}]" +
                            " with IP ADDRESS: {}.",
                    profile.getUuid(), deviceDto.getDEVICE_ID(), deviceDto.getDEVICE_NAME(), deviceDto.getOS(),
                    deviceDto.getOS_VERSION(), IP_ADDRESS);

            refreshToken = tokenService.generateRefreshToken(email, profile.getTag(), rolesByEmail, deviceDto.getDEVICE_ID());

            // If this true that means it is the first time logging. EXCEPT if he logged out from all devices.
            if (deviceSessionService.getDevicesForUser(email).isEmpty()){

                log.info("Account with UUID: {} tried to login from: [DeviceId: {}, DeviceName: {}, OS: {}, Version: {}]"
                                + " from Country: {} and City: {}.",
                        profile.getUuid(), deviceDto.getDEVICE_ID(), deviceDto.getDEVICE_NAME(), deviceDto.getOS(),
                        deviceDto.getOS_VERSION(), location.getCountry(), location.getCity());

                deviceSessionService.saveSession(profile, deviceDto.getDEVICE_ID(), deviceDto.getDEVICE_NAME(),
                        deviceDto.getOS(), deviceDto.getOS_VERSION(), IP_ADDRESS);
            }
            else {
                // This is a validation check, NOT YET LOGGED IN.
               emailService.validateNewDevice(email, deviceDto.getOS(), deviceDto.getDEVICE_NAME(), IP_ADDRESS);
               throw new BusinessException(ExceptionCode.DEVICE_NOT_RECOGNIZED,
                       "Suspicious Login in a new device: \n Device: " + deviceDto.getOS() +
                       " \n DeviceName: " + deviceDto.getDEVICE_NAME() + " \n Country: " +
                       location.getCountry() + " \n City: " + location.getCity());
            }
        }

        Jwt refreshJwt = Jwt.withTokenValue(refreshToken).build();

        String accessToken = tokenService.generateAccessToken(refreshJwt);

//        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByProfileId(profile.getId()).orElseThrow();
        userStatus.setStatus(EUserState.ONLINE);
        userStatusRepository.save(userStatus);

        log.info("User with this Email Address: [{}] Logged In Successfully. His UUID is: ", auth.getName());
        log.info("User with this Email Address: [{}] Has these Authorities.", auth.getAuthorities());

        ProfileDetails profileDetails = new ProfileDetails(profile.getUuid(), profile.getUsername(), profile.getTag(),
                profile.getUserStatus().getStatus().name(), profile.getAvatarUrl(), profile.getAboutMe(), profile.getQuote());

        return new AuthenticationResponse(accessToken, refreshToken, profileDetails);
    }

    // Bearer as Refresh Token not Access Token !!
    public String refreshAccessToken(@AuthenticationPrincipal Jwt jwt, String deviceId) {
        Account account = accountRepository.findByEmail(jwt.getSubject());
            try {
                if (tokenService.isRefreshTokenValid(jwt.getSubject(), deviceId,
                        jwt.getTokenValue())){
                    return tokenService.generateAccessToken(jwt);
                }
            } catch (ExpiredRefreshTokenException e) {
                return tokenService.generateAccessToken(jwt);
            }
        throw new RuntimeException("INTERNAL SERVER ERROR: SOMETHING WENT WRONG REFRESHING TOKEN");
    }

    public void registerUser(@Valid @Email(message = "Enter a valid email address.") String email, String password,
                             String username){

        if (emailService.isEmailRegistered(email))
            throw new BusinessException(ExceptionCode.ACCOUNT_ALREADY_REGISTERED);

        @Null(message = "BAD REQUEST: Something went wrong when adding new friend.")
        Account account = new Account(email, new BCryptPasswordEncoder().encode(password));
        account.getRoles().add(ERoles.USER);

        userService.createUser(account, username);

    }

    public void logout(String email, String deviceId){
        deviceSessionService.removeDevice(email, deviceId);
        refreshTokenStore.remove(email, deviceId);
    }

    public void registerUser(@Valid @Email(message = "Enter a valid email address.") String email, String password,
                             String username, ERoles role){

        @Null(message = "BAD REQUEST: Something went wrong when adding new friend.")
        Account account = new Account(email, password);
        account.setPassword(new BCryptPasswordEncoder().encode(account.getPassword()));
        account.getRoles().add(role);

        userService.createUser(account, username);

    }

    private Set<SimpleGrantedAuthority> mapRolesToAuthorities(Set<ERoles> userRoles) {
        return userRoles.stream()
                .map(roles -> new SimpleGrantedAuthority(roles.name()))
                .collect(Collectors.toSet());
    }
}
