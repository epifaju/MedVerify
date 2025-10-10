package com.medverify.exception;

/**
 * Exception levée lors d'erreurs d'appel aux API externes
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

