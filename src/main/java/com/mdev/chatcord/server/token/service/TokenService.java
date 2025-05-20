package com.mdev.chatcord.server.token.service;

import com.mdev.chatcord.server.exception.UnauthorizedException;
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
            throw new UnauthorizedException("Invalid refresh token: " + refreshJwt.getTokenValue());
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
//    @Value("${vault.url}")
//    private String vaultUrl;
//
//    @Value("${vault.token}")
//    private String vaultToken;
//
//    private PrivateKey privateKey;
//
//    private PublicKey publicKey;
//
//    @PostConstruct
//    public void init() throws Exception {
//        getPrivateKey();
//        getPublicKey();
//    }
//
//    private void getPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
//        String privateKeyPem = fetchSecretFromVault("chatcord/data/rsa", "private");
//
//        //log.info(privateKeyPem);
//
//        privateKeyPem = privateKeyPem
//                .replace("-----BEGIN PRIVATE KEY-----", "")
//                .replace("-----END PRIVATE KEY-----", "")
//                .replaceAll("\\s+", "");
//
//        //log.info(privateKeyPem);
//
//        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
//        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
//        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateSpec);
//    }
//
//    private String fetchSecretFromVault(String path, String key) {
//        String url = String.format("%s/v1/%s", vaultUrl, path);
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("X-Vault-Token", vaultToken);
//
//        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
//
//        ResponseEntity<Map> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                Map.class
//        );
//
//        Map data = (Map) ((Map) response.getBody().get("data")).get("data");
//        return data.get(key).toString();
//    }
//
//    public String generateAccessToken(Authentication authentication, User user, String deviceId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("deviceId", deviceId);
//        claims.put("uuid", user.getUuid());
//        claims.put("scope", createScope(authentication));
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .setClaims(claims)
//                .setIssuedAt(new Date())
//                .setExpiration(Date.from(Instant.now().plus(accessTokenTTL)))
//                .signWith(privateKey, SignatureAlgorithm.RS256)
//                .compact();
//    }
//
//    public String generateRefreshToken(Authentication authentication, User user, String deviceId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("deviceId", deviceId);
//        claims.put("uuid", user.getUuid());
//        claims.put("scope", createScope(authentication));
//
////        var jwtClaims = JwtClaimsSet.builder()
////                .issuer("http://localhost:8080")
////                .issuedAt(Instant.now())
////                .expiresAt(Instant.now().plusSeconds(60 * 30))
////                .subject(authentication.getName())
////                .claim("uuid", user.getUuid())
////                .claim("scope",createScope(authentication))
////                .claim("device-id", deviceId)
////                .build();
//
//        String jwt = Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(new Date())
//                .setExpiration(Date.from(Instant.now().plus(refreshTokenTTL)))
//                .signWith(privateKey, SignatureAlgorithm.RS256)
//                .setSubject(user.getEmail())
//                .compact();
//
//        storeRefreshToken(jwt, user.getEmail(), deviceId);
//        return jwt;
//    }
//
//    public boolean validateToken(String jwt) {
//        try {
//
//            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwt);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    public Claims parseToken(String jwt) {
//        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwt).getBody();
//    }
//
//    public void storeRefreshToken(String refreshJwtToken, String email, String deviceId) {
//        RefreshTokenData tokenData = new RefreshTokenData();
//        tokenData.setJwt(refreshJwtToken);
//        tokenData.setEmail(email);
//        tokenData.setDeviceId(deviceId);
//        tokenData.setExpiry(Instant.now().plus(refreshTokenTTL));
//
//        String key = buildRefreshKey(email, deviceId);
//        redisTemplate.opsForValue().set(key, tokenData, refreshTokenTTL);
//    }
//
//    public boolean validateRefreshToken(String email, String deviceId, String jwt) {
//        String key = buildRefreshKey(email, deviceId);
//        RefreshTokenData stored = (RefreshTokenData) redisTemplate.opsForValue().get(key);
//        if (stored == null)
//            throw new NullPointerException("Refresh Key is unavailable.");
//        if (Instant.now().isBefore(stored.getExpiry()))
//            throw new ExpiredRefreshTokenException("");
//        if (!stored.getJwt().equalsIgnoreCase(jwt))
//             throw new UnauthorizedException("The refresh key provided does not match.");
//        return true;
//    }
//
//    public void revokeRefreshToken(String email, String deviceId) {
//        redisTemplate.delete(buildRefreshKey(email, deviceId));
//    }
//
//    private String buildRefreshKey(String email, String deviceId) {
//        return "refresh_token:" + email + ":" + deviceId;
//    }
//
//    public RSAPublicKey getPublicKey() {
//
//        String publicKeyFromVault = fetchSecretFromVault("chatcord/data/rsa", "public");
//
//        String pem = publicKeyFromVault.replace("-----BEGIN PUBLIC KEY-----", "")
//                .replace("-----END PUBLIC KEY-----", "")
//                .replaceAll("\\s+", "");
//        byte[] pubBytes = Base64.getDecoder().decode(pem);
//        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);
//        try {
//            publicKey = KeyFactory.getInstance("RSA").generatePublic(pubSpec);
//        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//        return (RSAPublicKey) publicKey;
//    }
//
}
