//package com.mdev.chatcord.server.authentication.service;
//
//import com.mdev.chatcord.server.user.model.Account;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.jwt.JwtClaimsSet;
//import org.springframework.security.oauth2.jwt.JwtEncoder;
//import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
//
//import java.time.Instant;
//import java.util.stream.Collectors;
//
////@Service
//@RequiredArgsConstructor
//public class JwtService {
//
//    private final JwtEncoder jwtEncoder;
//
//    public String generateToken(Account account){
//        var claims = JwtClaimsSet.builder()
//                .issuer("http://localhost:8080")
//                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(60 * 30))
//                .subject(account.getEmail())
//                .claim("uuid", account.getUuid())
//                .claim("scope", account.getRoles().stream().map(Enum::name).collect(Collectors.joining(" ")))
//                .build();
//
//        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
//
//    }
//
//    public String generateToken(Authentication authentication, Account account){
//
//        var claims = JwtClaimsSet.builder()
//                .issuer("http://localhost:8080")
//                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(60 * 30))
//                .subject(authentication.getName())
//                .claim("uuid", account.getUuid())
//                .claim("scope", createScope(authentication))
//                .build();
//
//        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
//    }
//
//    private String createScope(Authentication authentication) {
//        return authentication.getAuthorities().stream()
//                .map(a -> a.getAuthority())
//                .collect (Collectors.joining(" "));
//    }
//
//
////    public String validateTokenAndGetUsername(String token){
////        try {
////            return Jwts.parserBuilder()
////                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody()
////                    .getSubject();
////        } catch (JwtException e){
////            return e.toString();
////        }
////    }
//}
