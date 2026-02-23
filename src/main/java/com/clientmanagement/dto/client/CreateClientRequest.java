package com.clientmanagement.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateClientRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    public String firstName;

    @Size(max = 100, message = "Second name must not exceed 100 characters")
    public String secondName;

    @NotBlank(message = "First surname is required")
    @Size(min = 2, max = 100, message = "First surname must be between 2 and 100 characters")
    public String firstSurname;

    @Size(max = 100, message = "Second surname must not exceed 100 characters")
    public String secondSurname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    public String email;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    public String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be valid (7-20 digits, may include +, spaces, hyphens, parentheses)")
    public String phone;

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be a valid ISO 3166-1 alpha-2 code (2 uppercase letters)")
    public String countryCode;

    public CreateClientRequest() {
    }

    public CreateClientRequest(String firstName, String secondName, String firstSurname,
                                String secondSurname, String email, String address,
                                String phone, String countryCode) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.countryCode = countryCode;
    }
}
