package com.mdev.chatcord.server.websocket.configuration;

import com.mdev.chatcord.server.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("access_token");

            if (token == null) {
                return false; // No token = reject connection
            }

            Jwt jwtFromTokenValue = tokenService.getJwtFromTokenValue(token);

            String email = jwtFromTokenValue.getSubject();
            String uuid = jwtFromTokenValue.getClaimAsString("uuid");
            String deviceId = jwtFromTokenValue.getClaimAsString("device-id");

            attributes.put("username", email); // OR UUID if you prefer
            servletRequest.getServletRequest().setAttribute("principal", new UUIDPrinciple(uuid));
            attributes.put("uuid", uuid);
            attributes.put("device-id", deviceId);

        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}