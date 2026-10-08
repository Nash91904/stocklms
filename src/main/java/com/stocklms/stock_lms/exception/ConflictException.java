package com.stocklms.stock_lms.exception;

/**
 * Thrown when an operation conflicts with current state or immutable constraints
 * (e.g. attempting to delete a record referenced in an immutable ledger, or duplicate watchlist entry).
 * Handled globally -> HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
