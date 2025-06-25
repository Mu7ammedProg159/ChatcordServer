package com.mdev.chatcord.server.websocket.friend.service;


import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FriendInteractionService {

    private final SimpMessagingTemplate messagingTemplate;

    public void updateFriendshipInRealtime(ProfileDetails owner, ProfileDetails acceptor) {
        ContactPreview contactPreview = getFriendship(owner.getUuid().toLowerCase(), acceptor.getUsername(),
                acceptor.getTag());

        log.info("{} with uuid: {} {} friendship with {} of uuid: {}",
                acceptor.getUsername(),
                acceptor.getUuid().toLowerCase(),
                contactPreview.getFriendStatus().name().toLowerCase(),
                owner.getUsername(),
                owner.getUuid().toLowerCase());

        messagingTemplate.convertAndSendToUser(
                owner.getUuid().toLowerCase(),
                "/queue/friendship.update",
                contactPreview);
    }
}
