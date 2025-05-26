package com.mdev.chatcord.server.chat.dto;

import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.user.model.Profile;

public record PrivateChatParticipants(Profile sender, Profile receiver, Friendship friendship){}
