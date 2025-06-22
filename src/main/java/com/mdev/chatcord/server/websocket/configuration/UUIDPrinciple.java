package com.mdev.chatcord.server.websocket.configuration;

import lombok.*;

import javax.security.auth.Subject;
import java.security.Principal;

@RequiredArgsConstructor
public class UUIDPrinciple implements Principal {
    private final String name;
    @Override
    public String getName() {
        return name;
    }
}
