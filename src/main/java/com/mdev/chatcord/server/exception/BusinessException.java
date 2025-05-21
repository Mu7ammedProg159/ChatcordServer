package com.mdev.chatcord.server.exception;

import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private final ExceptionCode exceptionCode;

    private final String message;

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.message = null;
    }

    public BusinessException(ExceptionCode exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    public String getMessage() {
        return (this.message != null) ? this.message : exceptionCode.getMessage();
    }
}
