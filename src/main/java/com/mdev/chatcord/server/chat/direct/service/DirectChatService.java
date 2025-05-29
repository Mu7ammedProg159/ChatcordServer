package com.mdev.chatcord.server.chat.direct.service;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.dto.ChatDTO;
import com.mdev.chatcord.server.chat.direct.dto.PrivateChatParticipants;
import com.mdev.chatcord.server.chat.core.dto.UnreadStatus;
import com.mdev.chatcord.server.communication.dto.ChatMemberDTO;
import com.mdev.chatcord.server.communication.model.ChatMember;
import com.mdev.chatcord.server.communication.model.ChatRole;
import com.mdev.chatcord.server.communication.model.Privilege;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.communication.repository.ChatRoleRepository;
import com.mdev.chatcord.server.communication.repository.PrivilegeRepository;
import com.mdev.chatcord.server.communication.service.PrivilegeType;
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DirectChatService {
    
    private final AccountRepository accountRepository;
    private final ProfileRepository userProfileRepository;
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoleRepository chatRoleRepository;
    private final PrivilegeRepository privilegeRepository;

    @Transactional(rollbackFor = Exception.class)
    public ChatDTO createPrivateChat(PrivateChatParticipants participants){
        Chat receiverChat = new Chat();
        receiverChat.setType(ChatType.PRIVATE);
        receiverChat.setCreatedAt(LocalDateTime.now());
        chatRepository.save(receiverChat);

        //ChatRole chatRole = createRole("Member", receiverChat);

        ChatMember senderChatMember = createDefaultChatMember(participants.sender(), receiverChat, null);
        ChatMember receiverChatMember = createDefaultChatMember(participants.receiver(), receiverChat, null);

        senderChatMember.setChat(receiverChat);
        receiverChatMember.setChat(receiverChat);

        List<ChatMember> chatMembers = new ArrayList<>(List.of(senderChatMember, receiverChatMember));
        receiverChat.setMembers(chatMembers);

        //chatMemberRepository.saveAll(chatMembers);
        try {
            chatRepository.save(receiverChat);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ Logs full trace
            throw e;
        }

        ChatMemberDTO senderChatMemberDTO = new ChatMemberDTO(participants.sender().getUsername(),
                participants.sender().getTag(), participants.sender().getAvatarUrl(),
                "Member");

        ChatMemberDTO receiverChatMemberDTO = new ChatMemberDTO(participants.receiver().getUsername(),
                participants.receiver().getTag(), participants.receiver().getAvatarUrl(),
                "Member");

        return ChatDTO.builder()
                .chatType(receiverChat.getType().name())
                .chatMembersDto(List.of(senderChatMemberDTO, receiverChatMemberDTO))
                .unreadStatus(new UnreadStatus(0, false, false))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ChatDTO retrieveConversation(String uuid, String receiverUsername, String receiverTag){

        Profile sender = userProfileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Profile receiver = userProfileRepository.findByUsernameAndTag(receiverUsername, receiverTag)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Chat chat = chatRepository.findPrivateChatBetweenUsers(
                sender.getId(),
                receiver.getId(),
                ChatType.PRIVATE
        ).orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        List<ChatMember> members = chat.getMembers();
        List<ChatMemberDTO> membersDTO = new ArrayList<>();

        for (ChatMember member: members){
            Profile profile = userProfileRepository.findByAccountId(member.getProfile().getId()).orElseThrow(()
                    -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

            membersDTO.add(new ChatMemberDTO(member.getProfile().getUsername(), member.getProfile().getTag(),
                    profile.getAvatarUrl(), "Member"));
        }

        Message lastMessage = null;
        if (!chat.getMessages().isEmpty()){
            lastMessage = chat.getMessages().stream().sorted(Comparator.comparing(Message::getSentAt).reversed())
                    .findFirst().orElseThrow(() -> new BusinessException(ExceptionCode.MESSAGE_NOT_FOUND));
        }

        return ChatDTO.builder()
                .chatType(chat.getType().name())
                .chatMembersDto(membersDTO)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .createdAt(chat.getCreatedAt())
                .lastMessageAt(lastMessage != null ? lastMessage.getSentAt() : null)
                .lastMessageSender(lastMessage != null ? lastMessage.getSender().getUsername() : null)
                .unreadStatus(new UnreadStatus(0, false, false))
                .build();
    }

    private ChatMember createDefaultChatMember(Profile chatProfile, Chat chat, ChatRole chatRole) {
        ChatMember chatMember = new ChatMember();
        chatMember.setProfile(chatProfile);
        chatMember.setChat(chat);
        chatMember.setMuted(false);
        chatMember.setRole(chatRole);
        chatMember.setPings(0);
        return chatMember;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatRole createRole(String roleName, Chat chat){

        if (!chatRoleRepository.existsByNameAndChat_Id(roleName, chat.getId())){
            Set<Privilege> privilege = Set.of(
                    new Privilege(PrivilegeType.SEND_MESSAGE),
                    new Privilege(PrivilegeType.EDIT_MESSAGE),
                    new Privilege(PrivilegeType.DELETE_MESSAGE),
                    new Privilege(PrivilegeType.REACT_MESSAGE)
            );
            privilegeRepository.saveAll(privilege);
            ChatRole chatRole = new ChatRole(roleName, privilege, chat);
            chatRoleRepository.save(chatRole);
            return chatRole;
        }

        return chatRoleRepository.findByNameAndChat_Id(roleName, chat.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.CHAT_NOT_FOUND));
    }
}
