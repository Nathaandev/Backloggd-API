package com.example.backloggd.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameAlreadyInUseException.class)
    public ResponseEntity<String> handleUsernameAlreadyInUse(UsernameAlreadyInUseException e) {
        logger.error("Username already in use.");
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(AlreadyPublishedAReviewException.class)
    public ResponseEntity<String> handleAlreadyPublishedAReview(AlreadyPublishedAReviewException e) {
        logger.error("Review already published.");
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Invalid request: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unhandled exception", e);
        return ResponseEntity.status(500).body("Internal server error.");
    }
}
