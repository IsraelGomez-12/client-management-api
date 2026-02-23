package com.clientmanagement.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class Client extends PanacheEntity {

    @Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
    public String uuid;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @Size(max = 100, message = "Second name must not exceed 100 characters")
    @Column(name = "second_name", length = 100)
    public String secondName;

    @NotBlank(message = "First surname is required")
    @Size(min = 2, max = 100, message = "First surname must be between 2 and 100 characters")
    @Column(name = "first_surname", nullable = false, length = 100)
    public String firstSurname;

    @Size(max = 100, message = "Second surname must not exceed 100 characters")
    @Column(name = "second_surname", length = 100)
    public String secondSurname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, length = 255)
    public String email;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    @Column(name = "address", nullable = false, length = 500)
    public String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be valid (7-20 digits, may include +, spaces, hyphens, parentheses)")
    @Column(name = "phone", nullable = false, length = 20)
    public String phone;

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be a valid ISO 3166-1 alpha-2 code (2 uppercase letters)")
    @Column(name = "country_code", nullable = false, length = 2)
    public String countryCode;

    @Column(name = "demonym", length = 100)
    public String demonym;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "active", nullable = false)
    public boolean active = true;

    public Client() {
    }

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName);
        if (secondName != null && !secondName.isBlank()) {
            fullName.append(" ").append(secondName);
        }
        fullName.append(" ").append(firstSurname);
        if (secondSurname != null && !secondSurname.isBlank()) {
            fullName.append(" ").append(secondSurname);
        }
        return fullName.toString();
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", firstSurname='" + firstSurname + '\'' +
                ", secondSurname='" + secondSurname + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", demonym='" + demonym + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
