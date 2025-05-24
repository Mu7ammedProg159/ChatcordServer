package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.dto.ChatDTO;
import com.mdev.chatcord.server.chat.dto.PrivateChatDTO;
import com.mdev.chatcord.server.chat.dto.PrivateChatParticipants;
import com.mdev.chatcord.server.chat.service.PrivateChatService;
import com.mdev.chatcord.server.exception.*;
import com.mdev.chatcord.server.friend.dto.FriendContactDTO;
import com.mdev.chatcord.server.friend.dto.FriendDTO;
import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.friend.repository.FriendRepository;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserStatusRepository userStatusRepository;
    private final FriendRepository friendRepository;

    private final PrivateChatService privateChatService;


    public PrivateChatDTO addFriend(@Valid String uuid, String friendUsername, String friendTag){

        User owner = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        User friend = userRepository.findByUsernameAndTag(friendUsername, friendTag).orElseThrow(
                () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        if (owner.getId().equals(friend.getId()))
            throw new BusinessException(ExceptionCode.CANNOT_ADD_SELF); // Works fine.

        if (!owner.isAccountNonLocked())
           throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED,
                   "Please verify your email address to use this feature."); // Not now ..

        UserProfile ownerProfile = profileRepository.findByUserId(owner.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        UserStatus ownerStatus = userStatusRepository.findByUserId(owner.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        UserProfile friendProfile = profileRepository.findByUserId(friend.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        UserStatus friendStatus = userStatusRepository.findByUserId(friend.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        if (friendRepository.existsByOwnerIdAndFriendId(owner.getId(), friend.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, "You already added "
                    + friendUsername + "#" + friendTag + " as a friend.");
        }
        else {

            Friend friendship = new Friend(owner, friend, EFriendStatus.PENDING, LocalDateTime.now());

            FriendDTO friendDTO = new FriendDTO(owner.getUsername(), owner.getTag(), friend.getUsername(), friend.getTag(),
                    friendProfile.getProfilePictureUrl(), friendship.getFriendStatus(), friendStatus.getStatus(),
                    friendship.getAddedAt());

            friendRepository.save(friendship);

            PrivateChatParticipants participants = new PrivateChatParticipants(
                    owner, friend, ownerProfile, friendProfile, friendship);

            ChatDTO chatDTO = privateChatService.createPrivateChat(participants);

            return new PrivateChatDTO(friendDTO, chatDTO);
        }
    }

    public List<FriendContactDTO> getAllFriends(String uuid) {
        User owner = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> friendships = friendRepository.findAllByOwnerId(owner.getId(), Pageable.unpaged()).getContent();
        List<FriendContactDTO> friendDTOList = new ArrayList<>();

        for (Friend friend : friendships) {
            UserProfile friendProfile = profileRepository.findByUserId(friend.getFriend().getId()).orElseThrow(
                    () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "User with ID " +
                            friend.getId() + " not found"));;
            //PrivateChat friendChat = pr
            friendDTOList.add(new FriendContactDTO(friend.getFriend().getUsername(), friend.getFriend().getTag(),
                    friendProfile.getProfilePictureUrl(), null, null, friend.getFriendStatus()));
        }
        return friendDTOList;
    }

    public List<FriendContactDTO> getAllPendingFriends(String uuid) {
        User currentUser = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> pendingFriendships = friendRepository.findAllByFriendStatusAndFriendId(EFriendStatus.PENDING,
                currentUser.getId(), Pageable.unpaged()).getContent();
        List<FriendContactDTO> friendDTOList = new ArrayList<>();

        for (Friend friend : pendingFriendships) {
            UserProfile friendProfile = profileRepository.findByUserId(friend.getOwner().getId()).orElseThrow(
                    () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "User with ID " + friend.getId()
                            + " not found"));;
            //PrivateChat friendChat = pr
            friendDTOList.add(new FriendContactDTO(friend.getOwner().getUsername(), friend.getOwner().getTag(),
                    friendProfile.getProfilePictureUrl(), null, null,
                    EFriendStatus.REQUESTED));
        }
        return friendDTOList;
    }

    public FriendContactDTO getFriend(String uuid, String username, String tag) {
        User owner = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.UUID_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        Friend friendship = friendRepository.findByFriendUsernameAndTag(owner.getId(), username, tag).orElseThrow(
                () -> new BusinessException(ExceptionCode.FRIENDSHIP_NOT_FOUND));

        if (owner.getId().equals(friendship.getFriend().getId()))
            throw new BusinessException(ExceptionCode.CANNOT_ADD_SELF); // Works fine.

        if (!owner.isAccountNonLocked())
            throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED,
                    "Please verify your email address to use this feature."); // Not now ..

        if (!friendRepository.existsByOwnerIdAndFriendId(owner.getId(), friendship.getFriend().getId()))
            throw new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "Account with username: "
                    + friendship.getFriend().getUsername() + " and tag: "
                    + friendship.getFriend().getTag() + " not exists."); //Check this

        FriendContactDTO friendDTO;

        UserProfile friendProfile = profileRepository.findByUserId(friendship.getFriend().getId()).orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
        //PrivateChat friendChat = pr
        friendDTO = new FriendContactDTO(friendship.getFriend().getUsername(), friendship.getFriend().getTag(),
                friendProfile.getProfilePictureUrl(), null, null, friendship.getFriendStatus());

        return friendDTO;
    }
}
