package com.mdev.chatcord.server.token.service;

import com.mdev.chatcord.server.token.model.RefreshTokenData;
import com.mdev.chatcord.server.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration refreshTokenTTL = Duration.ofDays(7);
    private final Duration accessTokenTTL = Duration.ofMinutes(15);

    @Value("${vault.url}")
    private String vaultUrl;

    @Value("${vault.token}")
    private String vaultToken;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        String privateKeyPem = fetchSecretFromVault("chatcord/data/rsa", "private");

        log.info(privateKeyPem);

        privateKeyPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        log.info(privateKeyPem);

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateSpec);
    }

    private String fetchSecretFromVault(String path, String key) {
        String url = String.format("%s/v1/%s", vaultUrl, path);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Vault-Token", vaultToken);

        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map data = (Map) ((Map) response.getBody().get("data")).get("data");
        return data.get(key).toString();
    }

    public String generateAccessToken(Authentication authentication, User user, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("deviceId", deviceId);
        claims.put("uuid", user.getUuid());
        claims.put("scope", createScope(authentication));
        return Jwts.builder()
                .setSubject(authentication.getName())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessTokenTTL)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication, User user, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("deviceId", deviceId);
        claims.put("uuid", user.getUuid());
        claims.put("scope", createScope(authentication));

        String jwt = Jwts.builder()
                .setSubject(authentication.getName())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessTokenTTL)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        storeRefreshToken(jwt, user.getEmail(), deviceId);
        return jwt;
    }

    public boolean validateToken(String jwt) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseToken(String jwt) {
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwt).getBody();
    }

    public void storeRefreshToken(String refreshJwtToken, String email, String deviceId) {
        RefreshTokenData tokenData = new RefreshTokenData();
        tokenData.setJwt(refreshJwtToken);
        tokenData.setEmail(email);
        tokenData.setDeviceId(deviceId);
        tokenData.setExpiry(Instant.now().plus(refreshTokenTTL));

        String key = buildRefreshKey(email, deviceId);
        redisTemplate.opsForValue().set(key, tokenData, refreshTokenTTL);
    }

    public boolean validateRefreshToken(String email, String deviceId, String jwt) {
        String key = buildRefreshKey(email, deviceId);
        RefreshTokenData stored = (RefreshTokenData) redisTemplate.opsForValue().get(key);
        return stored != null && stored.getJwt().equals(jwt) && Instant.now().isBefore(stored.getExpiry());
    }

    public void revokeRefreshToken(String email, String deviceId) {
        redisTemplate.delete(buildRefreshKey(email, deviceId));
    }

    private String buildRefreshKey(String email, String deviceId) {
        return "refresh_token:" + email + ":" + deviceId;
    }

    public RSAPublicKey getPublicKey() {

        String publicKeyFromVault = fetchSecretFromVault("chatcord/data/rsa", "public");

        String pem = publicKeyFromVault.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] pubBytes = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);
        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance("RSA").generatePublic(pubSpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return (RSAPublicKey) publicKey;
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
