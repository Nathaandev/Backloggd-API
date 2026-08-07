package com.example.backloggd.Exceptions;

public class IgdbApiException extends RuntimeException {
    public IgdbApiException(String message) {
        super(message);
    }

    public IgdbApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
