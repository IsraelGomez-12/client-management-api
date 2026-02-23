package com.clientmanagement.dto.client;

import com.clientmanagement.entity.Client;

import java.time.LocalDateTime;

public class ClientResponse {

    public String id;
    public String firstName;
    public String secondName;
    public String firstSurname;
    public String secondSurname;
    public String fullName;
    public String email;
    public String address;
    public String phone;
    public String countryCode;
    public String demonym;
    public boolean active;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public ClientResponse() {
    }

    public static ClientResponse fromEntity(Client client) {
        ClientResponse response = new ClientResponse();
        response.id = client.uuid;
        response.firstName = client.firstName;
        response.secondName = client.secondName;
        response.firstSurname = client.firstSurname;
        response.secondSurname = client.secondSurname;
        response.fullName = client.getFullName();
        response.email = client.email;
        response.address = client.address;
        response.phone = client.phone;
        response.countryCode = client.countryCode;
        response.demonym = client.demonym;
        response.active = client.active;
        response.createdAt = client.createdAt;
        response.updatedAt = client.updatedAt;
        return response;
    }
}
