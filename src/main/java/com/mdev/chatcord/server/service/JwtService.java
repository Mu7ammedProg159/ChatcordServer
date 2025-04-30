package com.mdev.chatcord.server.service;

import com.mdev.chatcord.server.model.ERoles;
import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateToken(User user){
        var claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8080")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60 * 30))
                .subject(user.getEmail())
                .claim("scope", user.getRoles().stream().map(Enum::name).collect(Collectors.joining(" ")))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    }

    public String generateTag(UserRepository userRepository){
        Random random = new Random();
        int id = random.nextInt(9000) + 1000;
        while (userRepository.existsByTag(String.valueOf(id))){
            id = random.nextInt(9000) + 1000;
        }

        return String.valueOf(id);
    }

    public String generateToken(Authentication authentication){
        var claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8080")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60 * 30))
                .subject(authentication.getName())
                .claim("scope", createScope(authentication))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String createScope(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect (Collectors.joining(" "));
    }


//    public String validateTokenAndGetUsername(String token){
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody()
//                    .getSubject();
//        } catch (JwtException e){
//            return e.toString();
//        }
//    }
}
