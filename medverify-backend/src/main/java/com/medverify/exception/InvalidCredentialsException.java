package com.medverify.exception;

/**
 * Exception levée lors de credentials invalides
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

