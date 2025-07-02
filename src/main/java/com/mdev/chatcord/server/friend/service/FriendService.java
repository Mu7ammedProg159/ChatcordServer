package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.friend.dto.ContactPair;
import com.mdev.chatcord.server.friend.dto.ContactPreview;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendService {

    ContactPair add(@Valid String uuid, String friendUsername, String friendTag);
    ContactPair accept(@Valid String uuid, String friendUsername, String friendTag);
    ContactPair decline(@Valid String uuid, String friendUsername, String friendTag);
    ContactPreview retrieveFriendship(String uuid, String friendUsername, String friendTag);
    ContactPreview retrieveFriendshipRequester(String uuid, String ownerUsername, String ownerTag);
    List<ContactPreview> retrieveAllFriendships(String uuid);
}
