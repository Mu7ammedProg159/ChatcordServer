package com.mdev.chatcord.server.friend.service;

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
import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendService {

    public static final String EMAIL_NOT_EXISTS = "INTERNAL SERVER ERROR: The email address using this feature does not exists.";

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserStatusRepository userStatusRepository;
    private final FriendRepository friendRepository;

    public FriendDTO addFriend(@Valid String uuid, String friendUsername, String friendTag){

        User owner = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new UsernameNotFoundException(""));

        User friend = userRepository.findByUsernameAndTag(friendUsername, friendTag).orElseThrow(FriendNotFoundException::new);

        if (owner.getId().equals(friend.getId()))
            throw new CannotAddSelfException("You cannot add yourself as a friend."); // Works fine.

        if (!owner.isAccountNonLocked())
           throw new LockedException("Please verify your email address to use this feature."); // Not now ..

        Optional<UserProfile> userProfile = profileRepository.findByUserId(friend.getId());
        Optional<UserStatus> userStatus = userStatusRepository.findByUserId(friend.getId());

        if (friendRepository.existsByOwnerIdAndFriendId(owner.getId(), friend.getId())){
            throw new FriendAlreadyAddedException("You already added " + friendUsername + "#" + friendTag + " as a friend.");
        }
        else {

            Friend friendship = new Friend(owner, friend, EFriendStatus.PENDING, LocalDateTime.now());

            FriendDTO friendDTO = new FriendDTO(owner.getUsername(), owner.getTag(), friend.getUsername(), friend.getTag(),
                    userProfile.get().getProfilePictureUrl(), friendship.getFriendStatus(), userStatus.get().getStatus(),
                    friendship.getAddedAt());

            friendRepository.save(friendship);
            return friendDTO;
        }
    }

    public List<FriendContactDTO> getAllFriends(String uuid) {
        User owner = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new UsernameNotFoundException(""));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> friendships = friendRepository.findAllByOwnerId(owner.getId(), Pageable.unpaged()).getContent();
        List<FriendContactDTO> friendDTOList = new ArrayList<>();

        for (Friend friend : friendships) {
            UserProfile friendProfile = profileRepository.findByUserId(friend.getFriend().getId()).orElseThrow(() -> new UsernameNotFoundException("User with ID " + friend.getId() + " not found"));;
            //PrivateChat friendChat = pr
            friendDTOList.add(new FriendContactDTO(friend.getFriend().getUsername(), friend.getFriend().getTag(),
                    friendProfile.getProfilePictureUrl(), null, null, friend.getFriendStatus()));
        }
        return friendDTOList;
    }

    public List<FriendContactDTO> getAllPendingFriends(String uuid) {
        User currentUser = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new UsernameNotFoundException(""));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> pendingFriendships = friendRepository.findAllByFriendStatusAndFriendId(EFriendStatus.PENDING, currentUser.getId(), Pageable.unpaged()).getContent();
        List<FriendContactDTO> friendDTOList = new ArrayList<>();

        for (Friend friend : pendingFriendships) {
            UserProfile friendProfile = profileRepository.findByUserId(friend.getOwner().getId()).orElseThrow(() -> new UsernameNotFoundException("User with ID " + friend.getId() + " not found"));;
            //PrivateChat friendChat = pr
            friendDTOList.add(new FriendContactDTO(friend.getOwner().getUsername(), friend.getOwner().getTag(),
                    friendProfile.getProfilePictureUrl(), null, null, EFriendStatus.REQUESTED));
        }
        return friendDTOList;
    }

    public FriendContactDTO getFriend(String uuid, String username, String tag) {
        User owner = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new UUIDNotFoundException(""));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        Friend friendship = friendRepository.findByFriendUsernameAndTag(owner.getId(), username, tag).orElseThrow(() -> new FriendNotFoundException(""));

        if (owner.getId().equals(friendship.getFriend().getId()))
            throw new IllegalArgumentException("You cannot add yourself as a friend."); // Works fine.

        if (!owner.isAccountNonLocked())
            throw new LockedException("Please verify your email address to use this feature."); // Not now ..

        if (!friendRepository.existsByOwnerIdAndFriendId(owner.getId(), friendship.getFriend().getId()))
            throw new FriendshipNotFoundException("Account with username: " + friendship.getFriend().getUsername() + " and tag: "
                    + friendship.getFriend().getTag() + " not exists."); //Check this

        FriendContactDTO friendDTO;

        UserProfile friendProfile = profileRepository.findByUserId(friendship.getFriend().getId()).orElseThrow(() -> new UsernameNotFoundException(""));
        //PrivateChat friendChat = pr
        friendDTO = new FriendContactDTO(friendship.getFriend().getUsername(), friendship.getFriend().getTag(),
                friendProfile.getProfilePictureUrl(), null, null, friendship.getFriendStatus());

        return friendDTO;
    }
}
