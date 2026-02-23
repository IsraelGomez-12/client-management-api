package com.clientmanagement.service;

import com.clientmanagement.client.CountryService;
import com.clientmanagement.dto.client.CreateClientRequest;
import com.clientmanagement.dto.client.UpdateClientRequest;
import com.clientmanagement.entity.Client;
import com.clientmanagement.exception.ClientNotFoundException;
import com.clientmanagement.exception.DuplicateEmailException;
import com.clientmanagement.exception.DuplicatePhoneException;
import com.clientmanagement.repository.ClientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles all client-related business logic including CRUD operations.
 * Duplicate checks (email, phone) only consider active clients to support soft delete.
 */
@ApplicationScoped
public class ClientService {

    private static final Logger LOG = Logger.getLogger(ClientService.class);

    @Inject
    ClientRepository clientRepository;

    @Inject
    CountryService countryService;

    @Transactional
    public Client createClient(CreateClientRequest request) {
        LOG.info("Creating new client with email: " + request.email);

        if (clientRepository.existsByEmail(request.email.toLowerCase())) {
            throw new DuplicateEmailException(request.email);
        }

        if (clientRepository.existsByPhone(request.phone.trim())) {
            throw new DuplicatePhoneException(request.phone);
        }

        Client client = new Client();
        client.firstName = request.firstName.trim();
        client.secondName = request.secondName != null ? request.secondName.trim() : null;
        client.firstSurname = request.firstSurname.trim();
        client.secondSurname = request.secondSurname != null ? request.secondSurname.trim() : null;
        client.email = request.email.toLowerCase().trim();
        client.address = request.address.trim();
        client.phone = request.phone.trim();
        client.countryCode = request.countryCode.toUpperCase().trim();

        // Validates the country code and fetches demonym in a single call
        client.demonym = countryService.getDemonym(client.countryCode);

        client.createdAt = LocalDateTime.now();
        client.updatedAt = LocalDateTime.now();

        clientRepository.persist(client);
        // Flush immediately so constraint violations surface before we return success
        clientRepository.flush();
        LOG.info("Successfully created client with id: " + client.id);

        return client;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAllOrderByCreatedAtDesc();
    }

    public List<Client> getClientsByCountry(String countryCode) {
        return clientRepository.findByCountryCode(countryCode.toUpperCase());
    }

    public Client getClientByUuid(String uuid) {
        return clientRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ClientNotFoundException(uuid));
    }

    /**
     * Updates only modifiable fields: email, address, phone, country.
     * If the country changes, the demonym is re-fetched from the external API.
     */
    @Transactional
    public Client updateClient(String uuid, UpdateClientRequest request) {
        LOG.info("Updating client with uuid: " + uuid);

        Client client = clientRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ClientNotFoundException(uuid));

        String newEmail = request.email.toLowerCase().trim();
        if (!newEmail.equals(client.email) &&
            clientRepository.existsByEmailAndUuidNot(newEmail, uuid)) {
            throw new DuplicateEmailException(request.email);
        }

        String newPhone = request.phone.trim();
        if (!newPhone.equals(client.phone) &&
            clientRepository.existsByPhoneAndUuidNot(newPhone, uuid)) {
            throw new DuplicatePhoneException(request.phone);
        }

        client.email = newEmail;
        client.address = request.address.trim();
        client.phone = newPhone;

        String newCountryCode = request.countryCode.toUpperCase().trim();
        if (!newCountryCode.equals(client.countryCode)) {
            // Fetch demonym first -- if the code is invalid this throws before we update anything
            client.demonym = countryService.getDemonym(newCountryCode);
            client.countryCode = newCountryCode;
        }

        client.updatedAt = LocalDateTime.now();

        LOG.info("Successfully updated client with uuid: " + uuid);
        return client;
    }

    /**
     * Soft-deletes a client (sets active = false). The record stays in the DB
     * but won't appear in queries anymore.
     */
    @Transactional
    public void deleteClient(String uuid) {
        LOG.info("Soft-deleting client with uuid: " + uuid);

        Client client = clientRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new ClientNotFoundException(uuid));

        client.active = false;
        client.updatedAt = LocalDateTime.now();
        LOG.info("Successfully soft-deleted client with uuid: " + uuid);
    }

    public long countClients() {
        return clientRepository.countActive();
    }

    public long countClientsByCountry(String countryCode) {
        return clientRepository.countActiveByCountryCode(countryCode);
    }
}
