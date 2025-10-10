package com.medverify.exception;

/**
 * Exception levée lorsqu'un compte est bloqué
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }
}

