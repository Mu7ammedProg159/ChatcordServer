package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.direct.model.PrivateChat;
import com.mdev.chatcord.server.chat.direct.repository.PrivateChatRepository;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.AlreadyBuiltException;
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
    private final PrivateChatRepository privateChatRepository;

    public FriendDTO addFriend(@Valid @Email(message = EMAIL_NOT_EXISTS) String ownerEmail, String friendUsername,
                               String friendTag){
        @Null(message = "UNAUTHORIZED: Something went wrong")
        User owner = userRepository.findByEmail(ownerEmail);

        @Null(message = "A user with this name and tag does not exists.")
        User friend = userRepository.findByUsernameAndTag(friendUsername, friendTag);


        if (Objects.equals(owner.getId(), friend.getId()))
            throw new IllegalArgumentException("You cannot add yourself as a friend.");

        if (!owner.isAccountNonLocked())
           throw new LockedException("Please verify your email address to use this feature.");

        if (!userRepository.existsByUsernameAndTag(friendUsername, friendTag))
            throw new UsernameNotFoundException("Account with username: " + friendUsername + " and tag: "
                    + friendTag + " not exists.");

        Optional<UserProfile> userProfile = profileRepository.findByUserId(friend.getId());
        Optional<UserStatus> userStatus = userStatusRepository.findByUserId(friend.getId());

        if (friendRepository.existsByOwnerIdAndFriendId(owner.getId(), friend.getId())){
            throw new AlreadyBuiltException("You added " + friend.getUsername()
                    + "#" + friend.getTag() + " already.");
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

    public List<FriendDTO> getAllFriends(String uuid, int page, int size){
        User owner = userRepository.findByUuid(UUID.fromString(uuid));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> friends = friendRepository.findAllByOwnerId(owner.getId(), Pageable.unpaged()).getContent();
        List<FriendContactDTO> friendDTOList = new ArrayList<>();
        for (Friend friend: friends){
            UserProfile friendProfile = profileRepository.findByUserId(friend.getId()).get();
            PrivateChat friendChat = pr
            friendDTOList.add(new FriendContactDTO(friend.getFriend().getUsername(), friend.getFriend().getTag(), friendProfile.getProfilePictureUrl(), ))
        }
    }
}
