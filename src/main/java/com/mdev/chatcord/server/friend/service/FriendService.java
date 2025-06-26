package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.core.dto.FriendshipPairDetails;
import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.communication.repository.ChatRoleRepository;
import com.mdev.chatcord.server.exception.*;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.user.dto.ProfileDetails;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final UserStatusRepository userStatusRepository;
    private final FriendshipRepository friendshipRepository;

    private final ChatRepository chatRepository;
    private final ChatRoleRepository chatRoleRepository;
    private final ChatMemberRepository chatMemberRepository;

    //private final SimpMessagingTemplate messagingTemplate;

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

        // Check if YOU added someone already
        if (friendshipRepository.existsByOwnerIdAndFriendId(ownerProfile.getId(), friendProfile.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, "You already added "
                    + friendUsername + "#" + friendTag + " as a friend.");
        }
        // Check if SOMEONE added you already
        else if (friendshipRepository.existsByOwnerIdAndFriendId(friendProfile.getId(), ownerProfile.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, friendProfile.getUsername() + "#"
                    + friendProfile.getTag() + " already requested friendship with you.");
        }
        else {

            Friendship friendship = new Friendship(ownerProfile, friendProfile, EFriendStatus.PENDING, LocalDateTime.now());
            friendshipRepository.save(friendship);

            Chat directChat = chatRepository.findPrivateChatBetweenUsers(ownerProfile.getId(),
                    friendProfile.getId(), ChatType.PRIVATE);

            EFriendStatus viewStatus;

            return new ContactPreview(
                    friendship.getFriend().getUuid(),
                    friendProfile.getUsername(),
                    friendProfile.getTag(),
                    friendProfile.getAvatarUrl(),
                    friendProfile.getAvatarHexColor(),
                    directChat != null ? (directChat.getLastMessageSent() != null ? directChat.getLastMessageSent().getMessage() : null) : null,
                    directChat != null ? (directChat.getLastMessageSent() != null ? directChat.getLastMessageSent().getSentAt() : friendship.getAddedAt()) : friendship.getAddedAt(),
                    directChat != null ? directChat.getLastMessageSent().getSender().getUsername() : null,
                    false,
                    friendship.getFriendStatus());
        }
    }

    // This retrieves all friendships
    @Transactional(rollbackFor = Exception.class)
    public List<ContactPreview> getAllFriends(String uuid) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Page<Friendship> friendships = friendshipRepository.findAllByProfileId(owner.getId(), Pageable.unpaged());

        List<ContactPreview> contacts = new ArrayList<>();

        List<Long> friendIds = friendships.stream()
                .map(f -> f.getFriend().getId())
                .toList();

        // Batching for better performance rather than Query N + 1 efficiency
        List<FriendshipPairDetails> friendshipPairDetails = chatRepository.findPrivateChatsWithFriendId(owner.getId(), friendIds);

        // To know which chat with what friend.
        Map<Long, Chat> chatByFriendId = friendshipPairDetails.stream()
                .collect(Collectors.toMap(
                        FriendshipPairDetails::getFriendId,
                        FriendshipPairDetails::getChat
                ));

        for (Friendship contact: friendships){

            String lastMessage = "No Messages sent yet.";
            LocalDateTime lastMessageAt = contact.getAddedAt();
            String lastMessageSender = "";

            Profile friend = contact.getFriend();
            EFriendStatus viewStatus = EFriendStatus.PENDING;

            DirectChat directChat = (DirectChat) chatByFriendId.get(contact.getFriend().getId());

            if (directChat != null && directChat.getLastMessageSent() != null){
                lastMessage = directChat.getLastMessageSent().getMessage();
                lastMessageAt = directChat.getLastMessageSent().getSentAt();
                lastMessageSender = directChat.getLastMessageSent().getSender().getUsername();
            }

            if (contact.getOwner().getId().equals(owner.getId())) {
                viewStatus = contact.getFriendStatus(); // PENDING, ACCEPTED
                friend = contact.getFriend();
            } else {
                // If it comes here means the friendee is retrieving his friends.
                // In short, the friend becomes the owner and the owner becomes the friend.
                if ( contact.getFriendStatus() == EFriendStatus.PENDING) {
                    viewStatus = EFriendStatus.REQUESTED; // UI-purpose only
                } else {
                    viewStatus =  contact.getFriendStatus(); // ACCEPTED or whatever
                }
                friend = contact.getOwner();
            }

            contacts.add(createContactPreview(friend, viewStatus, lastMessage, lastMessageAt, lastMessageSender));
        }

        return contacts;
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactPreview getFriendship(String uuid, String username, String tag) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.UUID_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        Friendship friendship = friendshipRepository.findByOwnerIdFriendUsernameAndTag(owner.getId(), username, tag).orElseThrow(
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

        DirectChat directChat = (DirectChat) chatRepository.findPrivateChatBetweenUsers(owner.getId(), friendship.getFriend().getId(),
                ChatType.PRIVATE);

        String lastMessage = "No Messages sent yet.";
        LocalDateTime lastMessageAt = friendship.getAddedAt();
        String lastMessageSender = "";

        if (directChat != null && directChat.getLastMessageSent() != null){
            lastMessage = directChat.getLastMessageSent().getMessage();
            lastMessageAt = directChat.getLastMessageSent().getSentAt();
            lastMessageSender = directChat.getLastMessageSent().getSender().getUsername();
        }

        return createContactPreview(friendship.getFriend(), friendship.getFriendStatus(), lastMessage, lastMessageAt, lastMessageSender);
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactPreview getFriendshipRequester(String uuid, String friendUsername, String friendTag) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.UUID_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        Friendship friendship = friendshipRepository.findByOwnerIdFriendUsernameAndTag(owner.getId(), friendUsername, friendTag).orElseThrow(
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

        DirectChat directChat = (DirectChat) chatRepository.findPrivateChatBetweenUsers(owner.getId(), friendship.getFriend().getId(),
                ChatType.PRIVATE);

        String lastMessage = "No Messages sent yet.";
        LocalDateTime lastMessageAt = friendship.getAddedAt();
        String lastMessageSender = "";

        if (directChat != null && directChat.getLastMessageSent() != null){
            lastMessage = directChat.getLastMessageSent().getMessage();
            lastMessageAt = directChat.getLastMessageSent().getSentAt();
            lastMessageSender = directChat.getLastMessageSent().getSender().getUsername();
        }

        return createContactPreview(friendship.getOwner(), EFriendStatus.REQUESTED,
                lastMessage, lastMessageAt, lastMessageSender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void declineFriend(String uuid, String username, String tag){
        Profile friend = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Profile owner = profileRepository.findByUsernameAndTag(username, tag)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Friendship friendship = friendshipRepository.findByOwnerIdFriendUsernameAndTag(owner.getId(), friend.getUsername(), friend.getTag())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));

        friendship.setFriendStatus(EFriendStatus.DECLINED);
        friendship.setDeleted(true);

        LocalDateTime currentDeclineTime = LocalDateTime.now();

        friendship.setDeclinedAt(currentDeclineTime);

        log.info("Friendship with id {} between user {} and user {} has been declined at {} successfully.",
                friendship.getId(),
                friendship.getOwner().getUsername(),
                friendship.getFriend().getUsername(),
                currentDeclineTime
        );

        friendshipRepository.save(friendship);

    }

     /** Since you are accepting means you are not the asker for friendship which means SOMEONE asked you to form
      * friendship, hence you are the friend, and he is the owner.
     **/

    @Transactional(rollbackFor = Exception.class)
    public void acceptFriend(String ownerUuid, String username, String tag) {

        Profile acceptor = profileRepository.findByUuid(UUID.fromString(ownerUuid)).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Profile owner = profileRepository.findByUsernameAndTag(username, tag).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Friendship friendship = friendshipRepository.findByOwnerIdFriendUsernameAndTag(owner.getId(),
                        acceptor.getUsername(), acceptor.getTag())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        friendship.setFriendStatus(EFriendStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        //updateFriendshipInRealtime(profileToDtoMapping(owner), profileToDtoMapping(acceptor));

    }

    private ContactPreview createContactPreview(Profile friend, EFriendStatus status, String lastMessage,
                                                LocalDateTime lastMessageAt, String lastMessageSender) {
        return ContactPreview.builder()
                .uuid(friend.getUuid())
                .displayName(friend.getUsername())
                .tag(friend.getTag())
                .avatarUrl(friend.getAvatarUrl())
                .avatarColor(friend.getAvatarHexColor())
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .lastMessageSender(lastMessageSender)
                .isGroup(false)
                .friendStatus(status)
                .build();
    }

}

//    @Transactional(rollbackFor = Exception.class)
//    public void removeFriend(String uuid, String username, String tag){
//        Profile friend = profileRepository.findByUuid(UUID.fromString(uuid))
//                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
//
//        Profile owner = profileRepository.findByUsernameAndTag(username, tag)
//                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
//
//        friendshipRepository.deleteFriendship(owner.getId(), friend.getId());
//
//        // ChatMember chatMember = chatMemberRepository.findByChatId(chat.getId()).orElseThrow();
//
////        if (chatMember.getRole() != null){
////            ChatRole chatRole = chatRoleRepository.findById(chatMember.getRole().getId()).orElseThrow();
////            chatRoleRepository.delete(chatRole);
////        }
//
////        friendRepository.deleteFriendship(owner.getId(), friend.getId());
////        chatMemberRepository.delete(chatMember);
//    }
