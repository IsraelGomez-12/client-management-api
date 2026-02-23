package com.clientmanagement.exception;

public class DuplicatePhoneException extends RuntimeException {

    private final String phone;

    public DuplicatePhoneException(String phone) {
        super("A client with phone number '" + phone + "' already exists");
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
}
