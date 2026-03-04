package com.beertestshop.exception;

/**
 * Исключение, возникающее при отказе в доступе.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
