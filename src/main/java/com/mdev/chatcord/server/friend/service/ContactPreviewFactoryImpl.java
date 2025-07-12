package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.chat.core.repository.ChatRepository;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.enums.EFriendStatus;
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.message.dto.MessageDTO;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.message.repository.MessageRepository;
import com.mdev.chatcord.server.message.service.EMessageStatus;
import com.mdev.chatcord.server.message.service.MessageFactory;
import com.mdev.chatcord.server.user.model.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactPreviewFactoryImpl implements ContactPreviewFactory{

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MessageFactory messageFactory;

    @Override
    public ContactPreview create(Profile viewer, Friendship friendship) {
        Profile friend = friendship.getOwner().getId().equals(viewer.getId())
                ? friendship.getFriend()
                : friendship.getOwner();

        Page<Message> messages = messageRepository.findChatMessagesByFriendship(friendship.getOwner().getId(),
                friendship.getFriend().getId(), ChatType.PRIVATE, Pageable.unpaged());

        int count = (int) messages.getContent().stream().filter(message -> message.getState().equals(EMessageStatus.DELIVERED)).count();

        MessageDTO lastMessage = messages.isEmpty()
                ? null
                : messageFactory.toMessageDTO(messages.getContent().get(messages.getContent().size() - 1));

        EFriendStatus status = determineViewStatus(viewer, friendship);

        return ContactPreview.builder()
                .uuid(friend.getUuid())
                .displayName(friend.getUsername())
                .tag(friend.getTag())
                .avatarUrl(friend.getAvatarUrl())
                .avatarColor(friend.getAvatarHexColor())
                .lastMessage(lastMessage)
                .unreadMessages(count)
                .isGroup(false)
                .friendStatus(status)
                .addedAt(friendship.getAddedAt())
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
