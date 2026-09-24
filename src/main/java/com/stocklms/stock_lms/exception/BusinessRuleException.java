package com.stocklms.stock_lms.exception;

/**
 * Thrown when a business rule is violated (e.g. duplicate ticker, insufficient cash).
 * Handled globally → HTTP 400.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
