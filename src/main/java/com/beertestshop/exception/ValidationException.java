package com.beertestshop.exception;

/**
 * Исключение, возникающее при ошибке валидации бизнес-логики.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
