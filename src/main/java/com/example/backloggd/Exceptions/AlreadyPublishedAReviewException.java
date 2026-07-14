package com.example.backloggd.Exceptions;

public class AlreadyPublishedAReviewException extends RuntimeException {
    public AlreadyPublishedAReviewException(String message) {
        super(message);
    }
}
