package com.stocklms.stock_lms.exception;

/**
 * Thrown when a requested resource cannot be found in the database.
 * Handled globally → HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
