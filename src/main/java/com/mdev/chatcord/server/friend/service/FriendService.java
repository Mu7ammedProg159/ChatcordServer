package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.chat.ChatRepository;
import com.mdev.chatcord.server.chat.ChatType;
import com.mdev.chatcord.server.chat.dto.ChatDTO;
import com.mdev.chatcord.server.chat.dto.PrivateChatDTO;
import com.mdev.chatcord.server.chat.dto.PrivateChatParticipants;
import com.mdev.chatcord.server.chat.service.PrivateChatService;
import com.mdev.chatcord.server.communication.model.ChatMember;
import com.mdev.chatcord.server.communication.model.ChatRole;
import com.mdev.chatcord.server.communication.repository.ChatMemberRepository;
import com.mdev.chatcord.server.communication.repository.ChatRoleRepository;
import com.mdev.chatcord.server.exception.*;
import com.mdev.chatcord.server.friend.dto.FriendContactDTO;
import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.friend.repository.FriendRepository;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.Profile;
import com.mdev.chatcord.server.user.model.UserStatus;
import com.mdev.chatcord.server.user.repository.ProfileRepository;
import com.mdev.chatcord.server.user.repository.AccountRepository;
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

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final UserStatusRepository userStatusRepository;
    private final FriendRepository friendRepository;

    private final ChatRepository chatRepository;
    private final ChatRoleRepository chatRoleRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final PrivateChatService privateChatService;


    public PrivateChatDTO addFriend(@Valid String uuid, String friendUsername, String friendTag){

        Account owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        Account friend = profileRepository.findByUsernameAndTag(friendUsername, friendTag).orElseThrow(
                () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        if (owner.getId().equals(friend.getId()))
            throw new BusinessException(ExceptionCode.CANNOT_ADD_SELF); // Works fine.

        if (!owner.isAccountNonLocked())
           throw new BusinessException(ExceptionCode.EMAIL_NOT_VERIFIED,
                   "Please verify your email address to use this feature."); // Not now ..

        Profile ownerProfile = profileRepository.findByAccountId(owner.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        UserStatus ownerStatus = userStatusRepository.findByUserId(owner.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Profile friendProfile = profileRepository.findByAccountId(friend.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        UserStatus friendStatus = userStatusRepository.findByUserId(friend.getId()).orElseThrow(()
                -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        if (friendRepository.existsByOwnerIdAndFriendId(owner.getId(), friend.getId())){
            throw new BusinessException(ExceptionCode.FRIEND_ALREADY_ADDED, "You already added "
                    + friendUsername + "#" + friendTag + " as a friend.");
        }
        else {

            Friend friendship = new Friend(owner, friend, EFriendStatus.PENDING, LocalDateTime.now());

            friendRepository.save(friendship);

            PrivateChatParticipants participants = new PrivateChatParticipants(
                    owner, friend, ownerProfile, friendProfile, friendship);

            ChatDTO chatDTO = privateChatService.createPrivateChat(participants);

            FriendContactDTO friendContactDTO = new FriendContactDTO(friend.getUsername(),
                    friend.getTag(), friendProfile.getAvatarUrl(), friendship.getFriendStatus());

            return new PrivateChatDTO(friendContactDTO, chatDTO);
        }
    }

    public List<PrivateChatDTO> getAllFriends(String uuid) {
        Account owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> friendships = friendRepository.findAllByOwnerId(owner.getId(), Pageable.unpaged()).getContent();
        List<FriendContactDTO> friendDTOList = new ArrayList<>();
        List<PrivateChatDTO> privateChatDTOList = new ArrayList<>();

        for (Friend friend : friendships) {
            Profile friendProfile = profileRepository.findByAccountId(friend.getFriend().getId()).orElseThrow(
                    () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "User with ID " +
                            friend.getId() + " not found"));;
            //PrivateChat friendChat = pr
            privateChatDTOList.add(new PrivateChatDTO(
                    new FriendContactDTO(
                            friend.getFriend().getUsername(), friend.getFriend().getTag(),
                            friendProfile.getAvatarUrl(), friend.getFriendStatus()
                    ),
                    privateChatService.retrieveConversation(
                            uuid, friend.getFriend().getUsername(),
                            friend.getFriend().getTag())
                    )
            );
        }

        return privateChatDTOList;
    }

    public List<PrivateChatDTO> getAllPendingFriends(String uuid) {
        Account currentAccount = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
                () -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));

        // In-Future if database became bigger overtime, must use pagination (+300 Records).
        List<Friend> pendingFriendships = friendRepository.findAllByFriendStatusAndFriendId(EFriendStatus.PENDING,
                currentAccount.getId(), Pageable.unpaged()).getContent();

        List<PrivateChatDTO> privateChatDTOList = new ArrayList<>();

        for (Friend friend : pendingFriendships) {
            Profile friendProfile = profileRepository.findByAccountId(friend.getOwner().getId()).orElseThrow(
                    () -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND, "User with ID " + friend.getId()
                            + " not found"));;
            //PrivateChat friendChat = pr
            privateChatDTOList.add(new PrivateChatDTO(
                            new FriendContactDTO(
                                    friend.getFriend().getUsername(), friend.getFriend().getTag(),
                                    friendProfile.getAvatarUrl(), EFriendStatus.PENDING
                            ),
                            privateChatService.retrieveConversation(
                                    uuid, friend.getFriend().getUsername(),
                                    friend.getFriend().getTag())
                    )
            );
        }
        return privateChatDTOList;
    }

    public PrivateChatDTO getFriend(String uuid, String username, String tag) {
        Account owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(
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

        FriendContactDTO friendContactDTO;

        Profile friendProfile = profileRepository.findByAccountId(friendship.getFriend().getId()).orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));
        //PrivateChat friendChat = pr
        friendContactDTO = new FriendContactDTO(friendship.getFriend().getUsername(), friendship.getFriend().getTag(),
                friendProfile.getAvatarUrl(), friendship.getFriendStatus());

        ChatDTO chatDTO = privateChatService.retrieveConversation(uuid, username, tag);

        return new PrivateChatDTO(friendContactDTO, chatDTO);
    }

    public void removeFriend(String uuid, String username, String tag){
        Account owner = profileRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new BusinessException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Account friend = profileRepository.findByUsernameAndTag(username, tag).orElseThrow(() -> new BusinessException(ExceptionCode.FRIEND_NOT_FOUND));

        Chat chat = chatRepository.findPrivateChatBetweenUsers(owner.getId(), friend.getId(), ChatType.PRIVATE).orElseThrow();
        ChatMember chatMember = chatMemberRepository.findByChatId(chat.getId()).orElseThrow();
        ChatRole chatRole = chatRoleRepository.findById(chatMember.getRole().getId()).orElseThrow();

        friendRepository.deleteFriendship(owner.getId(), friend.getId());
        chatRoleRepository.delete(chatRole);
        chatMemberRepository.delete(chatMember);
        chatRepository.delete(chat);

    }
}
