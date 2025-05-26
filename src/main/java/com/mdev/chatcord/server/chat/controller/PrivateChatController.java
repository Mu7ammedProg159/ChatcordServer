package com.mdev.chatcord.server.chat.controller;

import com.mdev.chatcord.server.chat.ChatRepository;
import com.mdev.chatcord.server.friend.repository.FriendRepository;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chat/privates")
@EnableMethodSecurity
public class PrivateChatController {

    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;
    private final ChatRepository chatRepository;

//    @PostMapping("/private")
//    public ResponseEntity<?> joinPrivateChat(@AuthenticationPrincipal Jwt jwt, @RequestParam String username, @RequestParam String tag, PrivateChatDTO privateChatDTO){
//        User sender = userRepository.findByUuid(UUID.fromString(jwt.getClaimAsString("uuid")))
//                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
//
//        User receiver = userRepository.findByUsernameAndTag(privateChatDTO.getFriendDTO().getUsername(),
//                privateChatDTO.getFriendDTO().getTag()).orElseThrow(()
//                -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
//
//        Chat chat = chatRepository.findPrivateChatBetweenUsers(sender.getId(), receiver.getId(), ChatType.PRIVATE)
//                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));
//
//        return ResponseEntity.ok()
//    }

}
