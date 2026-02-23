package com.clientmanagement.dto.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic wrapper for all API responses. Keeps a consistent structure
 * across success and error cases.
 */
public class ApiResponse<T> {

    public boolean success;
    public String message;
    public T data;
    public LocalDateTime timestamp;
    public List<FieldError> errors;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    public static ApiResponse<Void> noContent(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = null;
        return response;
    }

    public static ApiResponse<Void> error(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.data = null;
        return response;
    }

    public static ApiResponse<Void> validationError(String message, List<FieldError> fieldErrors) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.data = null;
        response.errors = fieldErrors;
        return response;
    }
}
