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
}
