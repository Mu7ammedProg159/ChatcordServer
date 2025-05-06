package com.mdev.chatcord.server.exception;

import jakarta.validation.constraints.Email;
import org.hibernate.annotations.NotFound;
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

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> handleLockedException(LockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body("Please verify your email address to login.");
    }
}