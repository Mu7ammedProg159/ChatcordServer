package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.friend.dto.FriendDTO;
import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.friend.repository.FriendRepository;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.UserRepository;
import com.mdev.chatcord.server.user.repository.UserStatusRepository;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserStatusRepository userStatusRepository;
    private final FriendRepository friendRepository;

    public FriendDTO addFriend(String ownerEmail, String friendUsername, String friendTag){
        @Null(message = "UNAUTHORIZED: Something went wrong")
        User owner = userRepository.findByEmail(ownerEmail);

        @Null(message = "A user with this name and tag does not exists.")
        User friend = userRepository.findByUsernameAndTag(friendUsername, friendTag);


        if (owner.getId() == friend.getId())
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
}
