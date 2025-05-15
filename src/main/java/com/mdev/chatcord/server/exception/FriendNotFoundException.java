package com.mdev.chatcord.server.exception;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException(String message) {
        super(message);
    }
    public FriendNotFoundException() {
        super();
    }
}
