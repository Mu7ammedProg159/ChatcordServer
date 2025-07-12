package com.mdev.chatcord.server.chat.direct.service;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.dto.ChatDTO;
import com.mdev.chatcord.server.chat.core.dto.ChatNotification;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
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
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.message.service.MessageFactory;
import com.mdev.chatcord.server.redis.model.MessageRedis;
import com.mdev.chatcord.server.redis.service.RedisMessageService;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DirectChatService {
    
    private final ProfileRepository profileRepository;
    private final FriendshipRepository friendshipRepository;

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;

    private final ChatRoleRepository chatRoleRepository;
    private final PrivilegeRepository privilegeRepository;

    private final MessageRepository messageRepository;
    private final MessageFactory messageFactory;
    private final RedisMessageService redisMessageService;

    @Transactional(rollbackFor = Exception.class)
    private ChatDTO createDirectChat(String requesterUUID, String receiverUUID){
        Profile sender = profileRepository.findByUuid(UUID.fromString(requesterUUID)).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Profile receiver = profileRepository.findByUuid(UUID.fromString(receiverUUID)).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Friendship friendship = friendshipRepository.findByOwnerIdFriendId(sender.getId(), receiver.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));

        DirectChat chat = new DirectChat();
        chat.setType(ChatType.PRIVATE);
        chat.setCreatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        //ChatRole chatRole = createRole("Member", receiverChat);

        ChatMember senderChatMember = createDefaultChatMember(sender, chat, null);
        ChatMember receiverChatMember = createDefaultChatMember(receiver, chat, null);

        senderChatMember.setChat(chat);
        receiverChatMember.setChat(chat);

        List<ChatMember> chatMembers = new ArrayList<>(List.of(senderChatMember, receiverChatMember));
        chat.setMembers(chatMembers);

        chatMemberRepository.saveAll(chatMembers);
        chatRepository.save(chat);

        ChatMemberDTO senderChatMemberDTO = new ChatMemberDTO(sender.getUuid().toString().toLowerCase(),
                sender.getUsername(), sender.getTag(), sender.getAvatarUrl(), sender.getAvatarHexColor(), "Member");

        ChatMemberDTO receiverChatMemberDTO = new ChatMemberDTO(receiver.getUuid().toString().toLowerCase(),
                receiver.getUsername(), receiver.getTag(), receiver.getAvatarUrl(), receiver.getAvatarHexColor(), "Member");

        return ChatDTO.builder()
                .uuid(UUID.randomUUID())
                .chatType(chat.getType().name())
                .chatMembersDto(List.of(senderChatMemberDTO, receiverChatMemberDTO))
                .chatNotification(new ChatNotification(false, false))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatDTO retrieveConversation(String senderUUID, String receiverUUID){

        Profile sender = profileRepository.findByUuid(UUID.fromString(senderUUID))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Profile receiver = profileRepository.findByUuid(UUID.fromString(receiverUUID))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        DirectChat chat = (DirectChat) chatRepository.findPrivateChatBetweenUsers(
                sender.getId(),
                receiver.getId(),
                ChatType.PRIVATE
        );

        if (chat == null)
            return createDirectChat(senderUUID, receiverUUID);

        List<ChatMember> members = chat.getMembers();
        List<ChatMemberDTO> membersDTO = new ArrayList<>();

        for (ChatMember member: members){

            Profile profile = member.getProfile();
            membersDTO.add(new ChatMemberDTO(profile.getUuid().toString().toLowerCase(),
                    profile.getUsername(), profile.getTag(),
                    profile.getAvatarUrl(), profile.getAvatarHexColor(), "Member"));
        }

        List<MessageDTO> messageDTOS = Stream.concat(
                        messageRepository.findByChatIdOrderBySentAtAsc(chat.getId(), Pageable.unpaged()).getContent().stream(),
                        Optional.ofNullable(redisMessageService.getBufferedMessages(chat.getId()))
                                .orElse(Collections.emptyList())
                                .stream()
                )
                .sorted(Comparator.comparing(Message::getSentAt))
                .map(messageFactory::toMessageDTO)
                .collect(Collectors.toList());

        return ChatDTO.builder()
                .uuid(chat.getUuid())
                .chatType(chat.getType().name())
                .chatMembersDto(membersDTO)
                .messages(messageDTOS)
                .createdAt(chat.getCreatedAt())
                .chatNotification(new ChatNotification(false, false))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
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
