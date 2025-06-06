package com.mdev.chatcord.server.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandleResolver {

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Map<String, Object>> handleRedisConnectionFailureException(RedisConnectionFailureException exception){
        return errorResponse("9001", "Connection Refused: Server is down for maintenance.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<Map<String, Object>> handleConnect(ConnectException ex) {
        return errorResponse("8001", "A backend service is unavailable. Try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiration(ExpiredJwtException ex) {
        return errorResponse("0002", "SESSION ACCESS TOKEN EXPIRED.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
        return errorResponse("0000", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

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
            case INVALID_EMAIL, ACCOUNT_NOT_FOUND, UUID_NOT_FOUND, FRIEND_NOT_FOUND, FRIENDSHIP_NOT_FOUND,
                 CHAT_NOT_FOUND, MESSAGE_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case EMAIL_NOT_VERIFIED -> HttpStatus.LOCKED;
            case EMAIL_ALREADY_VERIFIED, ACCOUNT_ALREADY_REGISTERED, FRIEND_ALREADY_ADDED -> HttpStatus.CONFLICT;
            case DEVICE_NOT_RECOGNIZED, INVALID_ACCESS_TOKEN, INVALID_REFRESH_TOKEN -> HttpStatus.FORBIDDEN;
            case CANNOT_ADD_SELF -> HttpStatus.METHOD_NOT_ALLOWED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private ResponseEntity<Map<String, Object>> errorResponse(String code, String message, HttpStatus status) {
        Map<String, Object> body = Map.of(
                "errorCode", code,
                "errorMessage", message
        );
        return ResponseEntity.status(status).body(body);
    }

}