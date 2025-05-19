package com.mdev.chatcord.server.authentication.service;

import com.mdev.chatcord.server.authentication.dto.RefreshDto;
import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import com.mdev.chatcord.server.device.service.IpLocationService;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.exception.AlreadyRegisteredException;
import com.mdev.chatcord.server.exception.ExpiredRefreshTokenException;
import com.mdev.chatcord.server.exception.NewDeviceAccessException;
import com.mdev.chatcord.server.exception.UnauthorizedException;
import com.mdev.chatcord.server.token.service.TokenService;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import com.mdev.chatcord.server.user.service.EUserState;
import com.mdev.chatcord.server.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final EmailService emailService;
    private final UserService userService;

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final ProfileRepository profileRepository;

    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    private final DeviceSessionService deviceSessionService;
    private final TokenService tokenService;
    private final IpLocationService locationService;



    public List<String> login(@Valid @Email(message = "Enter a valid email address.") String email, String password, DeviceDto deviceDto, String userAgent){

        if (!emailService.isEmailRegistered(email))
            throw new UsernameNotFoundException("Account with this Email Address not found.");

        @Null(message = "Email or password is invalid.")
        User user = userRepository.findByEmail(email);

        var auth = new UsernamePasswordAuthenticationToken(email, password, mapRolesToAuthorities(user));

        SecurityContextHolder.getContext().setAuthentication(auth);

        authenticationManager.authenticate(auth);

        if (!user.isEmailVerified()){
            if (otpService.canResendOtp(email) <= 0)
                emailService.validateEmailOtp(email);
            throw new LockedException("Please verify your email.");
        }

        if (userAgent != null){
            deviceDto.setOS(userAgent);
            deviceDto.setDEVICE_NAME("Web-Browser");
        }

        var location = locationService.getLocation(deviceDto.getLOCAL_IP_ADDRESS());
        if (!deviceSessionService.existsForUser(user, deviceDto.getDEVICE_ID())) {
            log.info("Account with UUID: {} tried to login from: [DeviceId: {}, DeviceName: {}, OS: {}, Version: {}]" +
                            " with IP ADDRESS: {}.",
                    user.getUuid(), deviceDto.getDEVICE_ID(), deviceDto.getDEVICE_NAME(), deviceDto.getOS(),
                    deviceDto.getOS_VERSION(), deviceDto.getLOCAL_IP_ADDRESS());

            // If this true that means it is the first time logging. EXCEPT if he logged out from all devices.
            if (deviceSessionService.getDevicesForUser(email).isEmpty()){


                log.info("Account with UUID: {} tried to login from: [DeviceId: {}, DeviceName: {}, OS: {}, Version: {}] from Country: {} and City: {}.",
                        user.getUuid(), deviceDto.getDEVICE_ID(), deviceDto.getDEVICE_NAME(), deviceDto.getOS(),
                        deviceDto.getOS_VERSION(), location.getCountry(), location.getCity());

                deviceSessionService.saveSession(user, deviceDto.getDEVICE_ID(), deviceDto.getDEVICE_NAME(),
                        deviceDto.getOS(), deviceDto.getOS_VERSION(), deviceDto.getLOCAL_IP_ADDRESS());
            }
            else {
                // This is a validation check, NOT YET LOGGED IN.
               emailService.validateNewDevice(email, deviceDto.getOS(), deviceDto.getDEVICE_NAME(), deviceDto.getLOCAL_IP_ADDRESS());
               throw new NewDeviceAccessException("Suspicious Login in a new device: \n Device: " + deviceDto.getOS() +
                       " \n DeviceName: " + deviceDto.getDEVICE_NAME() + " \n Country: " +
                       location.getCountry() + " \n City: " + location.getCity());
            }
        }

        String accessToken = tokenService.generateAccessToken(auth, user, deviceDto.getDEVICE_ID());
        String refreshToken = tokenService.generateRefreshToken(auth, user, deviceDto.getDEVICE_ID());

//        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElseThrow();
        userStatus.setStatus(EUserState.ONLINE);

        userStatusRepository.save(userStatus);

        log.info("User with this Email Address: [{}] Logged In Successfully. His UUID is: ", auth.getName());
        log.info("User with this Email Address: [{}] Has these Authorities.", auth.getAuthorities());

        return Arrays.asList(accessToken, refreshToken);
    }

    public List<String> refreshAccessToken(Authentication authentication, RefreshDto refreshDto) {
        User user = userRepository.findByEmail(authentication.getName());
        if (tokenService.validateToken(refreshDto.getRefreshToken())){
            try {
                if (tokenService.validateRefreshToken(authentication.getName(), refreshDto.getDeviceId(),
                    refreshDto.getRefreshToken())){
                    return Arrays.asList(tokenService.generateAccessToken(authentication, user, refreshDto.getDeviceId()));
                }
            } catch (ExpiredRefreshTokenException e) {
                return Arrays.asList(tokenService.generateAccessToken(authentication, user, refreshDto.getDeviceId()),
                        tokenService.generateRefreshToken(authentication, user, refreshDto.getDeviceId()));
            }
        }
        else{
            throw new UnauthorizedException("UNAUTHORIZED ACCESS: Invalid Refresh Key.");
        }
        throw new RuntimeException("INTERNAL SERVER ERROR: SOMETHING WENT WRONG REFRESHING TOKEN");
    }

    public void registerUser(@Valid @Email(message = "Enter a valid email address.") String email, String password, String username){

        if (emailService.isEmailRegistered(email))
            throw new AlreadyRegisteredException("Account with this email already registered.");

        @Null(message = "BAD REQUEST: Something went wrong when adding new friend.")
        User user = new User(email, new BCryptPasswordEncoder().encode(password), username);
        user.getRoles().add(ERoles.USER);

        userService.createUser(user);

    }

    public void registerUser(@Valid @Email(message = "Enter a valid email address.") String email, String password, String username, ERoles role){

        @Null(message = "BAD REQUEST: Something went wrong when adding new friend.")
        User user = new User(email, password, username);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.getRoles().add(role);

        userService.createUser(user);

    }

    private Set<SimpleGrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getRoles().stream()
                .map(roles -> new SimpleGrantedAuthority(roles.name()))
                .collect(Collectors.toSet());
    }
}
