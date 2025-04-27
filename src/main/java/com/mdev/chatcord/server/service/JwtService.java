package com.mdev.chatcord.server.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    private final String secretKey = "8183882574e9feef6ed4497de46af855b81984f21a1c9647abcc9d3d72f77ef243334758fd8f8ed474ff2316dfddf51d5290310a5d5d9699f1a097d77f7af264bf47dc97a34bf1e1e395bb8f997a4e9802f3b58941294df4c5bdb7b03946093f27e17ea4ebb81e270a74815a3d47a48932d2c7e9dfbcf9af9a964bf0f5bd7da61c997ed63433464ca89d640ee34e68afa7639cf517d23ffc85d287f9ead8425d53b564b9d1d9a876cf1d466bc468a7f5d77dd293576f1aae4b11674f96b27c40ca4522196dcebad2f587ab133749089f75a428401121890bcae6773007a1e12d8e0b223b8d4ef9db9828289391bbd1d99b2fd9a180f5094246904003e5491233";

    public String generateToken(String username, String tag){
        return Jwts.builder()
                .setSubject(username + "#" + tag)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String validateTokenAndGetUsername(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e){
            return e.toString();
        }
    }
}
