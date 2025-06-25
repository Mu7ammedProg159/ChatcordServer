package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendTaskScheduler {
    private final FriendshipRepository friendshipRepository;

    @Scheduled(fixedRate = 900_000)
    public void deleteDeclinedFriendships(){
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(60);
        int removedFriendships = friendshipRepository.deleteFriendship(cutoff);
        if (removedFriendships > 0)
            log.info("Cleaned {} declined friendships. All {} friendships have been gracefully deleted.",
                    removedFriendships, removedFriendships
            );
    }
}
