package com.mdev.chatcord.server.token.annotation;

import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.token.model.TokenType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthorityGuard {

    public boolean hasAccessToken(Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String tokenType = jwt.getClaimAsString("type");

        if (!TokenType.ACCESS.name().equalsIgnoreCase(tokenType)) {
            throw new BusinessException(ExceptionCode.INVALID_ACCESS_TOKEN);
        }

        return true;
    }

    public boolean hasRefreshToken(Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String tokenType = jwt.getClaimAsString("type");

        if (!TokenType.REFRESH.name().equalsIgnoreCase(tokenType)) {
            throw new BusinessException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }

        return true;
    }
}
