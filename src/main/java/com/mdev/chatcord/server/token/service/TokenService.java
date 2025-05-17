package com.mdev.chatcord.server.token.service;

import com.mdev.chatcord.server.token.model.RefreshTokenData;
import com.mdev.chatcord.server.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
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
public class TokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration refreshTokenTTL = Duration.ofDays(7);
    private final Duration accessTokenTTL = Duration.ofMinutes(15);

    @Value("${vault.url:http://localhost:8300}")
    private String vaultUrl;

    @Value("${vault.token:myroot}")
    private String vaultToken;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        String privateKeyPem = fetchSecretFromVault("chatcord/data/rsa", "private");

        privateKeyPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateSpec);

        // Optional: derive public key
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(privateKey.getEncoded()));
    }

    private String fetchSecretFromVault(String path, String key) {
        String url = String.format("%s/v1/%s", vaultUrl, path);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate
                .getForEntity(url, Map.class);

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
        return (RSAPublicKey) publicKey;
    }

    private String createScope(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect (Collectors.joining(" "));
    }
}
