package com.mdev.chatcord.server.exception;


import org.springframework.security.core.AuthenticationException;

public class BadEmaiException extends AuthenticationException {

    public BadEmaiException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BadEmaiException(String msg) {
        super(msg);
    }
}
