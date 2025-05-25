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
import com.mdev.chatcord.server.exception.BusinessException;
import com.mdev.chatcord.server.exception.ExceptionCode;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateChatService {
    
    private final UserRepository userRepository;
    private final ProfileRepository userProfileRepository;
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
                senderChatMember.getRole().getName());

        ChatMemberDTO receiverChatMemberDTO = new ChatMemberDTO(participants.receiver().getUsername(),
                participants.receiver().getTag(), participants.receiverProfile().getProfilePictureUrl(),
                receiverChatMember.getRole().getName());

        return ChatDTO.builder()
                .chatType(receiverChat.getType())
                .chatMembersDto(List.of(senderChatMemberDTO, receiverChatMemberDTO))
                .unreadStatus(new UnreadStatus(0, false, false))
                .build();
    }

    public ChatDTO retrieveConversation(String uuid, String receiverUsername, String receiverTag){

        User sender = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        User receiver = userRepository.findByUsernameAndTag(receiverUsername, receiverTag).orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Chat chat = chatRepository.findPrivateChatBetweenUsers(
                sender.getId(), receiver.getId(), ChatType.PRIVATE
        ).orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        List<ChatMember> members = chat.getMembers();
        List<ChatMemberDTO> membersDTO = null;

        for (ChatMember member: members){
            UserProfile userProfile = userProfileRepository.findByUserId(member.getUser().getId()).orElseThrow(()
                    -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
            membersDTO.add(new ChatMemberDTO(member.getUser().getUsername(), member.getUser().getTag(),
                    userProfile.getProfilePictureUrl(), member.getRole().getName()));
        }

        Message lastMessage = chat.getMessages().stream().sorted(Comparator.comparing(Message::getSentAt).reversed()).findFirst().orElseThrow();

        ChatDTO chatDTO = ChatDTO.builder()
                .chatType(chat.getType())
                .chatMembersDto(membersDTO)
                .lastMessage(lastMessage.getMessage())
                .createdAt(chat.getCreatedAt())
                .lastMessageAt(lastMessage.getSentAt())
                .lastMessageSender(lastMessage.getSender().getUsername())
                .unreadStatus(new UnreadStatus(0, false, false))
                .build();

        return chatDTO;
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
