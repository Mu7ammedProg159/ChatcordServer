package com.mdev.chatcord.server.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
  public ExpiredRefreshTokenException(String message) {
    super(message);
  }
}
