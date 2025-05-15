package com.mdev.chatcord.server.exception;

public class FriendAlreadyAddedException extends RuntimeException {
    public FriendAlreadyAddedException(String message) {
        super(message);
    }
}
