package com.mdev.chatcord.server.chat.dto;

import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.user.model.User;
import com.mdev.chatcord.server.user.model.UserProfile;

public record PrivateChatParticipants(User sender, User receiver, UserProfile senderProfile, UserProfile receiverProfile, Friend friendship){}
