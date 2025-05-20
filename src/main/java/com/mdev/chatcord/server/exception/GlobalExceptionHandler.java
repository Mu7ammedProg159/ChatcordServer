package com.mdev.chatcord.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account with this email address is not registered.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email address is not valid.");
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email or password is invalid.");
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> handleLockedException(LockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body("Please verify your email address to login.");
    }

    @ExceptionHandler(AlreadyVerifiedException.class)
    public ResponseEntity<String> handleAlreadyVerifiedException(AlreadyVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("The account is already verified.");
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<String> handleAlreadyRegisteredException(AlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Account with this email address already registered.");
    }

    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<String> handleFriendshipNotFoundException(FriendshipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(FriendAlreadyAddedException.class)
    public ResponseEntity<String> handleFriendAlreadyAddedException(FriendAlreadyAddedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(FriendNotFoundException.class)
    public ResponseEntity<String> handleFriendNotFoundException(FriendNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The friend you're trying to add does not exists.");
    }

    @ExceptionHandler(UUIDNotFoundException.class)
    public ResponseEntity<String> handleUUIDNotFoundException(UUIDNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account with this UUID does not exists.");
    }

    @ExceptionHandler(CannotAddSelfException.class)
    public ResponseEntity<String> handleCannotAddSelfException(CannotAddSelfException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You can't add yourself as a friend.");
    }

    @ExceptionHandler(NewDeviceAccessException.class)
    public ResponseEntity<String> handleNewDeviceAccessException(NewDeviceAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

}