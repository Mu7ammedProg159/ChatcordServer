package com.mdev.chatcord.server.chat.dto;

import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.user.model.Account;
import com.mdev.chatcord.server.user.model.UserProfile;

public record PrivateChatParticipants(Account sender, Account receiver, UserProfile senderProfile, UserProfile receiverProfile, Friend friendship){}
