package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.websocket.friend.service.FriendNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendTaskScheduler {
    private final FriendshipRepository friendshipRepository;
    private final FriendNotificationService notificationService;

    //900000
    @Scheduled(fixedRate = 900_000)
    public void deleteDeclinedFriendships(){
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(900);
        updateAndDeleteFriendships(cutoff);
        int removedFriendships = friendshipRepository.deleteFriendship(cutoff);
        if (removedFriendships > 0)

            log.info("Cleaned {} declined friendships. All {} friendships have been gracefully deleted.",
                    removedFriendships, removedFriendships
            );
    }

    public void updateAndDeleteFriendships(LocalDateTime cutoff){
        List<Friendship> friendships = friendshipRepository.findDeclinedFriendships(cutoff);
        for (Friendship friendship : friendships){
            notificationService.deleteFriendshipInRealtime(friendship.getOwner().getUuid().toString(),
                    friendship.getFriend().getUuid().toString());
        }
    }
}
