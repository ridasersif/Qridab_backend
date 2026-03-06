package com.qridaba.qridabaplatform.exception;

import com.qridaba.qridabaplatform.model.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        String fileName = "Unknown";
        int lineNumber = -1;


        for (StackTraceElement element : ex.getStackTrace()) {
            if (element.getClassName().startsWith("com.qridaba")) {
                fileName = element.getFileName();
                lineNumber = element.getLineNumber();
                break;
            }
        }

        return ErrorResponse.builder()
                .status(status.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .fileName(fileName)
                .lineNumber(lineNumber)
                .build();
    }

    // === Email already exists
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.CONFLICT, request), HttpStatus.CONFLICT);
    }

    // === Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.NOT_FOUND, request), HttpStatus.NOT_FOUND);
    }

    // === Duplicate Resource
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.CONFLICT, request), HttpStatus.CONFLICT);
    }

    // === Bad credentials
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        ErrorResponse error = buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // === Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
        errorResponse.setMessage("Validation failed");
        errorResponse.setErrors(errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // === Invalid Verification (400 Bad Request)
    @ExceptionHandler(InvalidVerificationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerification(InvalidVerificationException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request), HttpStatus.BAD_REQUEST);
    }

    // === JSON Parse Error / Bad Request
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
        errorResponse.setMessage("Malformed JSON request or invalid data format (e.g., invalid UUID)");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // === General Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // === Access Denied
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.FORBIDDEN, request), HttpStatus.FORBIDDEN);
    }

    // === Authentication
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request), HttpStatus.UNAUTHORIZED);
    }

    // === Role Not Allowed (403 Forbidden)
    @ExceptionHandler(RoleNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotAllowedException(RoleNotAllowedException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.FORBIDDEN, request), HttpStatus.FORBIDDEN);
    }

    // === Token Expired (401 Unauthorized)
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request), HttpStatus.UNAUTHORIZED);
    }


}