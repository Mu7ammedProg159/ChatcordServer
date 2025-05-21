package com.mdev.chatcord.server.token.service;

import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.redis.service.RefreshTokenStore;
import com.mdev.chatcord.server.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    //private final RedisTemplate<String, Object> redisTemplate;

    private final JwtEncoder jwtEncoder; // Provided by Spring Authorization Server
    private final RefreshTokenStore refreshTokenStore; // A Redis-backed service for storing refresh tokens.

    //@Value("application.chatcord.server.url")
    private final String issuer = "http://localhost:8080";

    private final Duration ACCESS_TOKEN_TTL_SECONDS = Duration.ofMinutes(15); // 15 minutes
    private final Duration REFRESH_TOKEN_TTL_SECONDS = Duration.ofDays(7); // 7 days

    public String generateAccessToken(Jwt refreshJwt, String deviceId) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_TTL_SECONDS.toSeconds()))
                .subject(refreshJwt.getSubject())
                .claim("device-id", deviceId)
                .claim("uuid", refreshJwt.getClaimAsString("uuid"))
                .claim("scope", refreshJwt.getClaimAsString("scope"))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

//    public String generateRefreshToken(Authentication authentication, String deviceId) {
//
//        JwtClaimsSet claims = JwtClaimsSet.builder()
//                .issuer(issuer)
//                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS.toSeconds()))
//                .subject(authentication.getName())
//                .claim("device-id", deviceId)
//                .claim("uuid", getUUIDFromJwt(authentication))
//                .claim("scope", createScope(authentication))
//                .build();
//
//        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
//        refreshTokenStore.save(authentication.getName(), deviceId, token, REFRESH_TOKEN_TTL_SECONDS.toSeconds());
//        return token;
//    }
    public String generateAccessTokenByUser(User user, String deviceId) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_TTL_SECONDS.toSeconds()))
                .subject(user.getEmail())
                .claim("device-id", deviceId)
                .claim("uuid", user.getUuid())
                .claim("scope", user.getRoles().stream().map(Enum::name).collect(Collectors.joining(" ")))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(User user, String deviceId) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS.toSeconds()))
                .subject(user.getEmail())
                .claim("device-id", deviceId)
                .claim("uuid", user.getUuid())
                .claim("scope", user.getRoles().stream().map(Enum::name).collect(Collectors.joining(" ")))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        refreshTokenStore.save(user.getEmail(), deviceId, token, REFRESH_TOKEN_TTL_SECONDS.toSeconds());
        return token;
    }

    public boolean isRefreshTokenValid(String subject, String deviceId, String token) {
        return refreshTokenStore.exists(subject, deviceId, token);
    }

    public void invalidateRefreshToken(String username, String deviceId) {
        refreshTokenStore.remove(username, deviceId);
    }

    public String renewAccessTokenFromRefreshToken(Jwt refreshJwt) {
        // Assuming JwtDecoder validated and parsed the refresh token externally
        String username = refreshJwt.getSubject();
        String deviceId = (String) refreshJwt.getClaimAsString("device-id"); // or extract from refreshToken claims

        if (!isRefreshTokenValid(username, deviceId, refreshJwt.getTokenValue())) {
            throw new BusinessException(ExceptionCode.UNAUTHORIZED, "Invalid refresh token: " + refreshJwt.getTokenValue());
        }

        // You may extract the uuid and scope from the decoded JWT claims if needed
        return generateAccessToken(refreshJwt, deviceId);
    }

    private String createScope(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect (Collectors.joining(" "));
    }

    public String getUUIDFromJwt(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return ((Jwt) principal).getClaim("uuid"); // or throw an exception
    }

}
