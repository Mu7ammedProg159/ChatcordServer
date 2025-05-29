package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.dto.ChatDTO;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.communication.repository.ChatRoleRepository;
import com.mdev.chatcord.server.exception.*;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final UserStatusRepository userStatusRepository;
    private final FriendshipRepository friendshipRepository;

    private final ChatRepository chatRepository;
    private final ChatRoleRepository chatRoleRepository;
    private final ChatMemberRepository chatMemberRepository;


    @Transactional(rollbackFor = Exception.class)
    public ContactPreview addFriend(@Valid String uuid, String friendUsername, String friendTag){

        Profile ownerProfile = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Profile friendProfile = profileRepository.findByUsernameAndTag(friendUsername, friendTag).orElseThrow(()
                -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        if (ownerProfile.getId().equals(friendProfile.getId()))
            throw new BusinessException(ExceptionCode.CANNOT_ADD_SELF); // Works fine.

        if (!ownerProfile.getAccount().isAccountNonLocked())
            throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED,
                    "Please verify your email address to use this feature."); // Not now ..

        if (friendshipRepository.existsByOwnerIdAndFriendId(ownerProfile.getId(), friendProfile.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, "You already added "
                    + friendUsername + "#" + friendTag + " as a friend.");
        }
        else {

            Friendship friendship = new Friendship(ownerProfile, friendProfile, EFriendStatus.PENDING, LocalDateTime.now());
            friendshipRepository.save(friendship);

            Chat directChat = chatRepository.findPrivateChatBetweenUsers(ownerProfile.getId(),
                    friendProfile.getId(), ChatType.PRIVATE).orElseThrow(() ->
                    new BusinessException(ExceptionCode.CHAT_NOT_FOUND));

            return new ContactPreview(
                    friendship.getFriend().getUuid(),
                    friendProfile.getUsername(),
                    friendProfile.getAvatarUrl(),
                    directChat != null ? (directChat.getLastMessageSent() != null ? directChat.getLastMessageSent().getMessage() : null) : null,
                    directChat != null ? (directChat.getLastMessageSent() != null ? directChat.getLastMessageSent().getSentAt() : null) : null,
                    directChat != null ? directChat.getLastMessageSender().getProfile().getUsername() : null,
                    false,
                    friendship.getFriendStatus());
        }
    }

    // This retrieves all friendships
    @Transactional(rollbackFor = Exception.class)
    public List<ContactPreview> getAllContacts(String uuid) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Page<Friendship> friendships = friendshipRepository.findAllByOwnerId(owner.getId(), Pageable.unpaged());
        List<ContactPreview> contacts = new ArrayList<>();


        // This will be a list of group chats or list of direct chats.
        var chat = chatRepository.findAllGroupChatsByProfileId(owner.getId());

        for (Friendship contact: friendships){
            contacts.add(ContactPreview.builder()
                            .displayName(contact.getFriend().getUsername(), )
                    .build());
        }

        return contacts;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ContactPreview> getAllPendingFriends(String uuid) {
        Profile currentProfile = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friendship> pendingFriendships = friendshipRepository.findAllByFriendStatusAndFriendId(EFriendStatus.PENDING,
                currentProfile.getId(), Pageable.unpaged()).getContent();

        List<ContactPreview> privateChatDTOList = new ArrayList<>();

        for (Friendship friendship : pendingFriendships) {
            Profile friendProfile = profileRepository.findById(friendship.getFriend().getId())
                    .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "Friendship with ID "
                            + friendship.getId() + " not found"));;

            privateChatDTOList.add(new ContactPreview(
                            new ContactPreview(
                                    friendship.getFriend().getUsername(), friendship.getFriend().getTag(),
                                    friendship.getFriend().getAvatarUrl(), EFriendStatus.REQUESTED
                            ),
                            privateChatService.retrieveConversation(
                                    uuid, friendship.getFriend().getUsername(),
                                    friendship.getFriend().getTag())
                    )
            );
        }
        return privateChatDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactPreview getFriendship(String uuid, String username, String tag) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.UUID_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        Friendship friendship = friendshipRepository.findByFriendUsernameAndTag(owner.getId(), username, tag).orElseThrow(
                () -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));

        if (owner.getId().equals(friendship.getFriend().getId()))
            throw new BusinessException(ExceptionCode.CANNOT_ADD_SELF); // Works fine.

        if (!owner.getAccount().isAccountNonLocked())
            throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED,
                    "Please verify your email address to use this feature."); // Not now ..

        if (!friendshipRepository.existsByOwnerIdAndFriendId(owner.getId(), friendship.getFriend().getId()))
            throw new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "Account with username: "
                    + friendship.getFriend().getUsername() + " and tag: "
                    + friendship.getFriend().getTag() + " not exists."); //Check this

        ContactPreview contactPreview;

        Profile friendProfile = profileRepository.findById(friendship.getFriend().getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        contactPreview = new ContactPreview(friendship.getFriend().getUsername(), friendship.getFriend().getTag(),
                friendship.getFriend().getAvatarUrl(), friendship.getFriendStatus());

        ChatDTO chatDTO = privateChatService.retrieveConversation(uuid, username, tag);

        return new ContactPreview(contactPreview, chatDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFriend(String uuid, String username, String tag){
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Profile friend = profileRepository.findByUsernameAndTag(username, tag)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Chat chat = chatRepository.findPrivateChatBetweenUsers(owner.getId(), friend.getId(), ChatType.PRIVATE)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CHAT_NOT_FOUND));

        // ChatMember chatMember = chatMemberRepository.findByChatId(chat.getId()).orElseThrow();

//        if (chatMember.getRole() != null){
//            ChatRole chatRole = chatRoleRepository.findById(chatMember.getRole().getId()).orElseThrow();
//            chatRoleRepository.delete(chatRole);
//        }

//        friendRepository.deleteFriendship(owner.getId(), friend.getId());
//        chatMemberRepository.delete(chatMember);
        chatRepository.delete(chat);

    }
}
