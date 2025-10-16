package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<String> handleApiException(ApiException ex, WebRequest request) {
        HttpStatus status = getStatusFromErrorType(ex.getType());
        return new ResponseEntity<>(ex.getMessage(), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        String message = "An unexpected internal server error occurred.";
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus getStatusFromErrorType(ApiException.ErrorType type) {
        switch (type) {
            case BAD_DATA:
                return HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED:
                return HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED:
                return HttpStatus.FORBIDDEN;
            case INTERNAL_ERROR:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}