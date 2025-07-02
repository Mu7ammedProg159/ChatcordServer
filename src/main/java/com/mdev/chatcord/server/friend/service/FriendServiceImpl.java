package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.core.dto.FriendshipPairDetails;
import com.mdev.chatcord.server.chat.core.model.Chat;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.direct.model.DirectChat;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.communication.repository.ChatRoleRepository;
import com.mdev.chatcord.server.exception.*;
import com.mdev.chatcord.server.friend.dto.ContactPair;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.enums.EFriendStatus;
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.friend.repository.FriendshipRepository;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {

    private final ProfileRepository profileRepository;
    private final FriendshipRepository friendshipRepository;
    private final ContactPreviewFactory contactPreviewFactory;

    //private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactPair add(String uuid, String friendUsername, String friendTag) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND)); // LostAside (Requester/User of this service).
        Profile friend = profileRepository.findByUsernameAndTag(friendUsername, friendTag).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND)); // NinjaBattosai (The friend meant to add)

        if (owner.getId().equals(friend.getId()))
            throw new BusinessException(ExceptionCode.CANNOT_ADD_SELF); // Works fine.

        if (!owner.getAccount().isAccountNonLocked())
            throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED,
                    "Please verify your email address to use this feature."); // Not now ..

        // Check if YOU added someone already
        if (friendshipRepository.existsByOwnerIdAndFriendId(owner.getId(), friend.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, "You already added "
                    + friendUsername + "#" + friendTag + " as a friend.");
        }
        // Check if SOMEONE added you already
        else if (friendshipRepository.existsByOwnerIdAndFriendId(friend.getId(), owner.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, friend.getUsername() + "#"
                    + friend.getTag() + " already requested friendship with you.");
        }

        Friendship friendship = new Friendship(owner, friend, EFriendStatus.PENDING, LocalDateTime.now());
        friendshipRepository.save(friendship);


        ContactPreview requester = contactPreviewFactory.create(friend, friendship);
        ContactPreview receiver = contactPreviewFactory.create(owner, friendship);


        return new ContactPair(requester, receiver);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactPair accept(String uuid, String friendUsername, String friendTag) {
        Profile requester = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND)); // NinjaBattosai (Acceptor)
        Profile receiver = profileRepository.findByUsernameAndTag(friendUsername, friendTag).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND)); // LostAside (Former of friendship)

        /*
        * The person who is accepting the friend is the one holding the UUID of authentication token.
        * This means that the friendship is formed not by the acceptor but by the one sent friendship,
        * which in this case is the receiver.
        */

        Friendship friendship = friendshipRepository
                .findByOwnerIdFriendUsernameAndTag(receiver.getId(), requester.getUsername(), requester.getTag())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));

        friendship.setFriendStatus(EFriendStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        //eventPublisher.publishEvent(new FriendAcceptedEvent(this, requester, receiver));
        ContactPreview acceptor = contactPreviewFactory.create(receiver, friendship);
        ContactPreview receiverContact = contactPreviewFactory.create(requester, friendship);

        return new ContactPair(acceptor, receiverContact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactPair decline(String uuid, String friendUsername, String friendTag) {

        Profile requester = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Profile receiver = profileRepository.findByUsernameAndTag(friendUsername, friendTag)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
        Friendship friendship = friendshipRepository.findByOwnerIdFriendUsernameAndTag(receiver.getId(), requester.getUsername(), requester.getTag())
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

        ContactPreview requesterContact = contactPreviewFactory.create(receiver, friendship);
        ContactPreview receiverContact = contactPreviewFactory.create(requester, friendship);

        return new ContactPair(requesterContact, receiverContact);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactPreview retrieveFriendship(String uuid, String friendUsername, String friendTag) {
        Profile owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Friendship friendship = friendshipRepository
                .findByOwnerIdFriendUsernameAndTag(owner.getId(), friendUsername, friendTag)
                .orElseThrow(() -> new IllegalStateException("Friendship not found"));

        return contactPreviewFactory.create(owner, friendship);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactPreview retrieveFriendshipRequester(String uuid, String ownerUsername, String ownerTag) {
        Profile requester = profileRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException(ExceptionCode.UUID_NOT_FOUND));
        Profile owner = profileRepository.findByUsernameAndTag(ownerUsername, ownerTag)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        Friendship friendship = friendshipRepository.findByOwnerIdFriendUsernameAndTag(requester.getId(), owner.getUsername(),
                owner.getTag()).orElseThrow(() -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));

        return contactPreviewFactory.create(owner, friendship);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ContactPreview> retrieveAllFriendships(String uuid) {
        Profile user = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        return friendshipRepository.findAllByProfileId(user.getId(), Pageable.unpaged())
                .stream()
                .map(f -> contactPreviewFactory.create(user, f))
                .toList();
    }
}
