package com.clientmanagement.exception;

public class CountryServiceException extends RuntimeException {

    private final String countryCode;

    public CountryServiceException(String countryCode, String message) {
        super("Error fetching country data for code '" + countryCode + "': " + message);
        this.countryCode = countryCode;
    }

    public CountryServiceException(String countryCode, Throwable cause) {
        super("Error fetching country data for code '" + countryCode + "'", cause);
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
