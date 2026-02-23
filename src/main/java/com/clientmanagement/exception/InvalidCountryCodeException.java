package com.clientmanagement.exception;

public class InvalidCountryCodeException extends RuntimeException {

    private final String countryCode;

    public InvalidCountryCodeException(String countryCode) {
        super("Invalid country code: '" + countryCode + "'. Must be a valid ISO 3166-1 alpha-2 code.");
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
