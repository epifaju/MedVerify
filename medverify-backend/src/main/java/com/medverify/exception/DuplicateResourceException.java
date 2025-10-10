package com.medverify.exception;

/**
 * Exception levée lors d'une duplication de ressource
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

