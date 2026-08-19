package com.salarymanagement.exception;

/**
 * Raised when a request is syntactically valid but violates a domain rule, for example a
 * salary change dated before the record it would supersede.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
