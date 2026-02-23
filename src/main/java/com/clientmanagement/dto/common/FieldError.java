package com.clientmanagement.dto.common;

public class FieldError {

    public String field;
    public String message;
    public Object rejectedValue;

    public FieldError() {
    }

    public FieldError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }
}
