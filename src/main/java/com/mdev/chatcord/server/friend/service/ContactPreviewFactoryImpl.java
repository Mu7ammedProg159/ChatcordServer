package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.enums.EFriendStatus;
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.user.model.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactPreviewFactoryImpl implements ContactPreviewFactory{

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Override
    public ContactPreview create(Profile viewer, Friendship friendship) {
        Profile friend = friendship.getOwner().getId().equals(viewer.getId())
                ? friendship.getFriend()
                : friendship.getOwner();

        Page<Message> messages = messageRepository.findChatMessagesByFriendship(friendship.getOwner().getId(),
                friendship.getFriend().getId(), ChatType.PRIVATE, Pageable.unpaged());

        String lastMessage = "No Messages sent yet.";
        LocalDateTime lastMessageAt = friendship.getAddedAt();
        String lastMessageSender = "";

        if (!messages.isEmpty()) {
            lastMessage = messages.getContent().get(0).getMessage();
            lastMessageAt = messages.getContent().get(0).getSentAt();
            lastMessageSender = messages.getContent().get(0).getSender().getUsername() + "#"
                    + messages.getContent().get(0).getSender().getTag();
        }

        EFriendStatus status = determineViewStatus(viewer, friendship);

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

    private EFriendStatus determineViewStatus(Profile current, Friendship friendship) {
        if (friendship.getOwner().getId().equals(current.getId())) {
            return friendship.getFriendStatus();
        } else if (friendship.getFriendStatus() == EFriendStatus.PENDING) {
            return EFriendStatus.REQUESTED;
        } else {
            return friendship.getFriendStatus();
        }
    }
}
