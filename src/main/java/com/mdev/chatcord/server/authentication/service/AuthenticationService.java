package com.mdev.chatcord.server.authentication.service;

import com.mdev.chatcord.server.device.dto.DeviceDto;
import com.mdev.chatcord.server.device.service.DeviceSessionService;
import com.mdev.chatcord.server.device.service.EPlatform;
import com.mdev.chatcord.server.device.service.IpLocationService;
import com.mdev.chatcord.server.email.service.EmailService;
import com.mdev.chatcord.server.email.service.OtpService;
import com.mdev.chatcord.server.exception.AlreadyRegisteredException;
import com.mdev.chatcord.server.exception.NewDeviceAccessException;
import com.mdev.chatcord.server.token.service.TokenService;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import com.mdev.chatcord.server.user.service.EUserState;
import com.mdev.chatcord.server.user.service.UserService;
import com.nimbusds.jose.util.JSONArrayUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        if (userAgent != null)
            deviceDto.setOs(userAgent);

        if (!deviceSessionService.existsForUser(user, deviceDto.getDeviceId())) {
            log.info("Account with UUID: {} tried to login from: [DeviceId: {}, DeviceName: {}, OS: {}, Version: {}]" +
                            " with IP ADDRESS: {}.",
                    user.getUuid(), deviceDto.getDeviceId(), deviceDto.getDeviceName(), deviceDto.getOs(),
                    deviceDto.getOsVersion(), deviceDto.getIp());

            // If this true that means it is the first time logging. EXCEPT if he logged out from all devices.
            if (deviceSessionService.getDevicesForUser(email).isEmpty()){

                var location = locationService.getLocation(deviceDto.getIp());

                log.info("Account with UUID: {} tried to login from: [DeviceId: {}, DeviceName: {}, OS: {}, Version: {}] from Country: {} and City: {}.",
                        user.getUuid(), deviceDto.getDeviceId(), deviceDto.getDeviceName(), deviceDto.getOs(),
                        deviceDto.getOsVersion(), location.getCountry(), location.getCity());

                deviceSessionService.saveSession(user, deviceDto.getDeviceId(), deviceDto.getDeviceName(),
                        deviceDto.getOs(), deviceDto.getOsVersion(), deviceDto.getIp());
            }
            else {
                // This is a validation check, NOT YET LOGGED IN.
               emailService.validateNewDevice(email, deviceDto.getOs(), deviceDto.getDeviceName(), deviceDto.getIp());
               throw new NewDeviceAccessException("Suspicious Login in a new device: \n Device: " + deviceDto.getOs() +
                       " \n DeviceName: " + deviceDto.getDeviceName() + " \n Country: " +
                       locationService.getLocation(deviceDto.getIp()));
            }
        }

        String accessToken = tokenService.generateAccessToken(auth, user, deviceDto.getDeviceId());
        String refreshToken = tokenService.generateRefreshToken(auth, user, deviceDto.getDeviceId());

//        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElseThrow();
        userStatus.setStatus(EUserState.ONLINE);

        userStatusRepository.save(userStatus);

        log.info("User with this Email Address: [{}] Logged In Successfully. His UUID is: ", auth.getName());
        log.info("User with this Email Address: [{}] Has these Authorities.", auth.getAuthorities());

        return Arrays.asList(accessToken, refreshToken);
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
