package com.mdev.chatcord.server.websocket.user.controller;

import com.mdev.chatcord.server.user.dto.UserStatusDetails;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.service.EUserState;
import com.mdev.chatcord.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserStatusController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProfileRepository profileRepository;
    private final UserService userService;

    @MessageMapping("/users/status/change")
    public void changeUserState(Message<?> wsMessage, Principal principal, EUserState state){

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(wsMessage);
        String uuid = (String) Objects.requireNonNull(accessor.getSessionAttributes()).get("uuid");
        userService.changeStatus(UUID.fromString(uuid), state);

        Set<UUID> relations = userService.retrieveAllUserRelations(uuid);

        UserStatusDetails statusDetails = new UserStatusDetails(uuid, state);

        for (UUID userUUID: relations){
            messagingTemplate.convertAndSendToUser(String.valueOf(userUUID), "/queue/status", statusDetails);
        }
    }
}
