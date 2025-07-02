package com.mdev.chatcord.server.friend.service;

import com.mdev.chatcord.server.friend.dto.ContactPreview;
import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.user.model.Profile;

public interface ContactPreviewFactory {
    ContactPreview create(Profile viewer, Friendship friendship);
}
