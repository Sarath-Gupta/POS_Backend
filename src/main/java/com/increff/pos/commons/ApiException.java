package com.increff.pos.commons;

public class ApiException extends Exception {

    // --- Define Specific Error Types ---
    public enum ErrorType {
        BAD_DATA,          // 400: Validation failure, uniqueness failure, bad format
        UNAUTHENTICATED,   // 401: Missing or invalid credentials/token
        UNAUTHORIZED,      // 403: User role lacks necessary permissions (e.g., Operator trying Supervisor action)
        INTERNAL_ERROR     // 500: Server crash, unexpected failure
    }

    private final ErrorType type;

    // Constructor for standard business logic errors (default to BAD_DATA)
    public ApiException(String message) {
        super(message);
        this.type = ErrorType.BAD_DATA;
    }

    // Constructor for specific error types (like 401, 403)
    public ApiException(String message, ErrorType type) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}