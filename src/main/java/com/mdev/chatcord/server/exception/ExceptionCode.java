package com.mdev.chatcord.server.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    UNAUTHORIZED("0001", "UNAUTHORIZED: SESSION INVALID."),
    INVALID_ACCESS_TOKEN("0003", "Invalid Session Key. Session expired, please re-login."),
    INVALID_REFRESH_TOKEN("0004", "Invalid Reloaded Session Key. Session expired, please re-login."),
    INVALID_EMAIL("1001", "Email address does not exists."),
    ACCOUNT_NOT_FOUND("1002","Account with this email address is not registered."),
    UUID_NOT_FOUND("1003", "Account with this UUID does not exists."),
    INVALID_CREDENTIALS("1004", "Email or password is invalid."),
    EMAIL_NOT_VERIFIED("1005", "Please verify your email address to login."),
    EMAIL_ALREADY_VERIFIED("1006", "The account is already verified."),
    ACCOUNT_ALREADY_REGISTERED("1007", "Account with this email address already registered."),
    DEVICE_NOT_RECOGNIZED("2001", "Suspicious new device attempt."),
    FRIEND_NOT_FOUND("3001", "The friend you're trying to add does not exists."),
    FRIENDSHIP_NOT_FOUND("3002", "Friend is not added."),
    CANNOT_ADD_SELF("3003", "You can't add yourself as a friend."),
    FRIEND_ALREADY_ADDED("3004", "You cannot add existing friend."),
    CHAT_NOT_FOUND("4001", "You cannot view this chat. Unable to fetch chat."),
    MESSAGE_NOT_FOUND("5001", "Unable to find this message.");

    private final String code;
    private final String message;
    ExceptionCode(String code, String message){
        this.code = code;
        this.message = message;
    }
}
