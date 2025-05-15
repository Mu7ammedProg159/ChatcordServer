package com.mdev.chatcord.server.exception;

public class CannotAddSelfException extends RuntimeException {
    public CannotAddSelfException(String message) {
        super(message);
    }
}
