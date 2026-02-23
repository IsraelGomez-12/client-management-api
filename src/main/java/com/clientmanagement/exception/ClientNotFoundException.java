package com.clientmanagement.exception;

public class ClientNotFoundException extends RuntimeException {

    private final String clientUuid;

    public ClientNotFoundException(String clientUuid) {
        super("Client not found with id: " + clientUuid);
        this.clientUuid = clientUuid;
    }

    public String getClientUuid() {
        return clientUuid;
    }
}
