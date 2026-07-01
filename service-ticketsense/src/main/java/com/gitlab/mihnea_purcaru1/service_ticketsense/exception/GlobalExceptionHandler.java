package com.gitlab.mihnea_purcaru1.service_ticketsense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(
            BadCredentialsException exception,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Invalid username or password.",
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception exception,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "An unexpected error occurred: " + exception.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
