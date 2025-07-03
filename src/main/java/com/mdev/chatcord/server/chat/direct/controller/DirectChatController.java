package com.mdev.chatcord.server.chat.direct.controller;

import com.mdev.chatcord.server.chat.core.dto.ChatDTO;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.direct.service.DirectChatService;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.token.annotation.RequiredAccessToken;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/request/chat/privates")
@EnableMethodSecurity
public class DirectChatController {

    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatRepository chatRepository;
    private final DirectChatService directChatService;

    @GetMapping("/private")
    @RequiredAccessToken
    public ResponseEntity<?> startDirectChatSession(@AuthenticationPrincipal Jwt jwt, @RequestParam String receiver){
        String senderUUID = jwt.getClaimAsString("uuid");
        ChatDTO chatDTO = directChatService.retrieveConversation(senderUUID, receiver);
        return ResponseEntity.ok(chatDTO);
    }

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
