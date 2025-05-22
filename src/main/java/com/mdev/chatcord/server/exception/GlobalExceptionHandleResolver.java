package com.mdev.chatcord.server.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandleResolver {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");

        Map<String, Object> body = Map.of(
                "errorCode", "1001", // custom code for all validation problems
                "errorMessage", errorMessage
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        Map<String, Object> body = Map.of(
                "errorCode", ex.getExceptionCode().getCode(),
                "errorMessage", ex.getMessage()
        );

        HttpStatus status = mapCodeToStatus(ex.getExceptionCode());
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus mapCodeToStatus(ExceptionCode code) {
        return switch (code) {
            case UNAUTHORIZED, INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case INVALID_EMAIL, ACCOUNT_NOT_FOUND, UUID_NOT_FOUND, FRIEND_NOT_FOUND, FRIENDSHIP_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case EMAIL_NOT_VERIFIED -> HttpStatus.LOCKED;
            case EMAIL_ALREADY_VERIFIED, ACCOUNT_ALREADY_REGISTERED, FRIEND_ALREADY_ADDED -> HttpStatus.CONFLICT;
            case DEVICE_NOT_RECOGNIZED -> HttpStatus.FORBIDDEN;
            case CANNOT_ADD_SELF -> HttpStatus.METHOD_NOT_ALLOWED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

}