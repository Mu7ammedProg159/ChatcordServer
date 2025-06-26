package com.mdev.chatcord.server.websocket.friend.service;


import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.service.FriendService;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FriendNotifierService {

    private final SimpMessagingTemplate messagingTemplate;

    public void addFriendshipInRealtime(ContactPreview requesterContact, ContactPreview receiverContact){

        log.info("{} with uuid: {} requested friendship with {} of uuid: {}",
                requesterContact.getDisplayName(),
                requesterContact.getUuid().toString().toLowerCase(),
                receiverContact.getDisplayName(),
                receiverContact.getUuid().toString().toLowerCase());

        // Reason why we passed requester is because we are saying here,
        // Hey, I am requesting friendship to you (receiver.uuid), these are my information (requesterContact).
        messagingTemplate.convertAndSendToUser(
                receiverContact.getUuid().toString().toLowerCase(),
                "/queue/friendship.add",
                requesterContact);
    }

    public void updateFriendshipInRealtime(ContactPreview requester, ContactPreview receiver) {

        log.info("{} with uuid: {} {} friendship with {} of uuid: {}",
                receiver.getDisplayName(),
                receiver.getUuid().toString().toLowerCase(),
                requester.getFriendStatus().name().toLowerCase(),
                requester.getDisplayName(),
                requester.getUuid().toString().toLowerCase());

        messagingTemplate.convertAndSendToUser(
                receiver.getUuid().toString().toLowerCase(),
                "/queue/friendship.update",
                requester);
    }
}
