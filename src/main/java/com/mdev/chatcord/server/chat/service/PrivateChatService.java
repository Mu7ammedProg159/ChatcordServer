package com.mdev.chatcord.server.chat.service;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.chat.ChatRepository;
import com.mdev.chatcord.server.chat.ChatType;
import com.mdev.chatcord.server.chat.dto.ChatDTO;
import com.mdev.chatcord.server.chat.dto.PrivateChatParticipants;
import com.mdev.chatcord.server.chat.dto.UnreadStatus;
import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.communication.dto.ChatRoleDTO;
import com.mdev.chatcord.server.communication.model.ChatMember;
import com.mdev.chatcord.server.communication.model.ChatRole;
import com.mdev.chatcord.server.communication.model.Privilege;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.communication.repository.ChatRoleRepository;
import com.mdev.chatcord.server.communication.repository.PrivilegeRepository;
import com.mdev.chatcord.server.communication.service.PrivilegeType;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PrivateChatService {
    
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoleRepository chatRoleRepository;
    private final PrivilegeRepository privilegeRepository;

    public ChatDTO createPrivateChat(PrivateChatParticipants participants){
        Chat receiverChat = new Chat();
        receiverChat.setType(ChatType.PRIVATE);
        receiverChat.setCreatedAt(LocalDateTime.now());
        //chatRepository.save(receiverChat);

        ChatMember senderChatMember = createDefaultChatMember(participants.sender());
        ChatMember receiverChatMember = createDefaultChatMember(participants.receiver());

        List<ChatMember> chatMembers = List.of(senderChatMember, receiverChatMember);
        chatMemberRepository.saveAll(chatMembers);

        participants.sender().setParticipation(chatMembers);
        participants.receiver().setParticipation(chatMembers);
        userRepository.save(participants.sender());
        userRepository.save(participants.receiver());

        receiverChat.setMembers(chatMembers);
        chatRepository.save(receiverChat);

        ChatMemberDTO senderChatMemberDTO = new ChatMemberDTO(participants.sender().getUsername(),
                participants.sender().getTag(), participants.senderProfile().getProfilePictureUrl(),
                new ChatRoleDTO(senderChatMember.getRole().getName()));

        ChatMemberDTO receiverChatMemberDTO = new ChatMemberDTO(participants.receiver().getUsername(),
                participants.receiver().getTag(), participants.receiverProfile().getProfilePictureUrl(),
                new ChatRoleDTO(receiverChatMember.getRole().getName()));

        return ChatDTO.builder()
                .chatType(receiverChat.getType())
                .relationship(participants.friendship().getFriendStatus())
                .chatMembersDto(List.of(senderChatMemberDTO, receiverChatMemberDTO))
                .unreadStatus(new UnreadStatus(0, false, false))
                .build();
    }

    private ChatMember createDefaultChatMember(User chatUser) {
        ChatMember chatMember = new ChatMember();
        chatMember.setUser(chatUser);
        chatMember.setMuted(false);

        // Default privileges for a member in a chat
        Set<Privilege> privilege = Set.of(
                new Privilege(PrivilegeType.SEND_MESSAGE),
                new Privilege(PrivilegeType.EDIT_MESSAGE),
                new Privilege(PrivilegeType.DELETE_MESSAGE),
                new Privilege(PrivilegeType.REACT_MESSAGE)
        );

        privilegeRepository.saveAll(privilege);

        ChatRole chatRole = new ChatRole("Member", privilege);
        chatRoleRepository.save(chatRole);

        chatMember.setRole(chatRole);
        chatMember.setPings(0);

        return chatMember;
    }
}
