package com.clientmanagement.service;

import com.clientmanagement.client.CountryService;
import com.clientmanagement.dto.client.CreateClientRequest;
import com.clientmanagement.dto.client.UpdateClientRequest;
import com.clientmanagement.entity.Client;
import com.clientmanagement.exception.ClientNotFoundException;
import com.clientmanagement.exception.DuplicateEmailException;
import com.clientmanagement.exception.DuplicatePhoneException;
import com.clientmanagement.exception.InvalidCountryCodeException;
import com.clientmanagement.repository.ClientRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ClientServiceTest {

    @Inject
    ClientService clientService;

    @InjectMock
    ClientRepository clientRepository;

    @InjectMock
    CountryService countryService;

    private CreateClientRequest validCreateRequest;
    private UpdateClientRequest validUpdateRequest;
    private Client existingClient;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateClientRequest();
        validCreateRequest.firstName = "John";
        validCreateRequest.secondName = "Michael";
        validCreateRequest.firstSurname = "Doe";
        validCreateRequest.secondSurname = "Smith";
        validCreateRequest.email = "john.doe@example.com";
        validCreateRequest.address = "123 Main Street, City";
        validCreateRequest.phone = "+1-555-123-4567";
        validCreateRequest.countryCode = "US";

        validUpdateRequest = new UpdateClientRequest();
        validUpdateRequest.email = "john.updated@example.com";
        validUpdateRequest.address = "456 New Avenue, Town";
        validUpdateRequest.phone = "+1-555-987-6543";
        validUpdateRequest.countryCode = "MX";

        existingClient = new Client();
        existingClient.id = 1L;
        existingClient.uuid = "7b2a4e8f-3c1d-4a5b-9e6f-8d7c2b1a0e3f";
        existingClient.firstName = "John";
        existingClient.secondName = "Michael";
        existingClient.firstSurname = "Doe";
        existingClient.secondSurname = "Smith";
        existingClient.email = "john.doe@example.com";
        existingClient.address = "123 Main Street, City";
        existingClient.phone = "+1-555-123-4567";
        existingClient.countryCode = "US";
        existingClient.demonym = "American";
        existingClient.active = true;
        existingClient.createdAt = LocalDateTime.now().minusDays(1);
        existingClient.updatedAt = LocalDateTime.now().minusDays(1);
    }

    @Nested
    @DisplayName("Create Client")
    class CreateClientTests {

        @Test
        @DisplayName("creates client with valid data")
        void shouldCreateClientSuccessfully() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(false);
            when(countryService.getDemonym("US")).thenReturn("American");
            doNothing().when(clientRepository).persist(any(Client.class));

            Client result = clientService.createClient(validCreateRequest);

            assertNotNull(result);
            assertEquals("John", result.firstName);
            assertEquals("Michael", result.secondName);
            assertEquals("Doe", result.firstSurname);
            assertEquals("Smith", result.secondSurname);
            assertEquals("john.doe@example.com", result.email);
            assertEquals("123 Main Street, City", result.address);
            assertEquals("+1-555-123-4567", result.phone);
            assertEquals("US", result.countryCode);
            assertEquals("American", result.demonym);
            assertNotNull(result.createdAt);
            assertNotNull(result.updatedAt);

            verify(clientRepository).persist(any(Client.class));
        }

        @Test
        @DisplayName("rejects duplicate email")
        void shouldThrowExceptionWhenEmailExists() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(true);

            DuplicateEmailException exception = assertThrows(
                    DuplicateEmailException.class,
                    () -> clientService.createClient(validCreateRequest)
            );

            assertEquals(validCreateRequest.email, exception.getEmail());
            verify(clientRepository, never()).persist(any(Client.class));
        }

        @Test
        @DisplayName("rejects duplicate phone")
        void shouldThrowExceptionWhenPhoneExists() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(true);

            DuplicatePhoneException exception = assertThrows(
                    DuplicatePhoneException.class,
                    () -> clientService.createClient(validCreateRequest)
            );

            assertEquals(validCreateRequest.phone, exception.getPhone());
            verify(clientRepository, never()).persist(any(Client.class));
        }

        @Test
        @DisplayName("rejects invalid country code")
        void shouldThrowExceptionForInvalidCountryCode() {
            validCreateRequest.countryCode = "RD";
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(false);
            when(countryService.getDemonym("RD")).thenThrow(new InvalidCountryCodeException("RD"));

            assertThrows(InvalidCountryCodeException.class,
                    () -> clientService.createClient(validCreateRequest));

            verify(clientRepository, never()).persist(any(Client.class));
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            validCreateRequest.email = "JOHN.DOE@EXAMPLE.COM";
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(false);
            when(countryService.getDemonym(anyString())).thenReturn("American");
            doNothing().when(clientRepository).persist(any(Client.class));

            Client result = clientService.createClient(validCreateRequest);

            assertEquals("john.doe@example.com", result.email);
        }

        @Test
        @DisplayName("normalizes country code to uppercase")
        void shouldNormalizeCountryCodeToUppercase() {
            validCreateRequest.countryCode = "us";
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(false);
            when(countryService.getDemonym("US")).thenReturn("American");
            doNothing().when(clientRepository).persist(any(Client.class));

            Client result = clientService.createClient(validCreateRequest);

            assertEquals("US", result.countryCode);
        }
    }

    @Nested
    @DisplayName("Get Clients")
    class GetClientTests {

        @Test
        @DisplayName("returns all active clients")
        void shouldGetAllClients() {
            Client client2 = new Client();
            client2.id = 2L;
            client2.uuid = "a1f5d9c3-6e2b-4d8a-b7c4-5f3e1a9d6b2c";
            client2.firstName = "Jane";
            client2.firstSurname = "Doe";
            client2.email = "jane@example.com";
            client2.active = true;

            List<Client> clients = Arrays.asList(existingClient, client2);
            when(clientRepository.findAllOrderByCreatedAtDesc()).thenReturn(clients);

            List<Client> result = clientService.getAllClients();

            assertEquals(2, result.size());
            verify(clientRepository).findAllOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("returns empty list when no clients")
        void shouldReturnEmptyListWhenNoClients() {
            when(clientRepository.findAllOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            List<Client> result = clientService.getAllClients();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("finds client by UUID")
        void shouldGetClientByUuid() {
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));

            Client result = clientService.getClientByUuid(existingClient.uuid);

            assertNotNull(result);
            assertEquals(existingClient.uuid, result.uuid);
            assertTrue(result.active);
            assertEquals("John", result.firstName);
        }

        @Test
        @DisplayName("throws 404 when UUID not found")
        void shouldThrowExceptionWhenClientNotFound() {
            String nonExistentUuid = "00000000-0000-0000-0000-000000000000";
            when(clientRepository.findActiveByUuid(nonExistentUuid)).thenReturn(Optional.empty());

            ClientNotFoundException exception = assertThrows(
                    ClientNotFoundException.class,
                    () -> clientService.getClientByUuid(nonExistentUuid)
            );

            assertEquals(nonExistentUuid, exception.getClientUuid());
        }

        @Test
        @DisplayName("filters by country code")
        void shouldGetClientsByCountry() {
            List<Client> usClients = Collections.singletonList(existingClient);
            when(clientRepository.findByCountryCode("US")).thenReturn(usClients);

            List<Client> result = clientService.getClientsByCountry("US");

            assertEquals(1, result.size());
            assertEquals("US", result.get(0).countryCode);
        }

        @Test
        @DisplayName("normalizes country code on filter")
        void shouldNormalizeCountryCodeOnGet() {
            when(clientRepository.findByCountryCode("US")).thenReturn(Collections.singletonList(existingClient));

            clientService.getClientsByCountry("us");

            verify(clientRepository).findByCountryCode("US");
        }
    }

    @Nested
    @DisplayName("Update Client")
    class UpdateClientTests {

        @Test
        @DisplayName("updates fields successfully")
        void shouldUpdateClientSuccessfully() {
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(clientRepository.existsByPhoneAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(countryService.getDemonym("MX")).thenReturn("Mexican");

            Client result = clientService.updateClient(existingClient.uuid, validUpdateRequest);

            assertEquals("john.updated@example.com", result.email);
            assertEquals("456 New Avenue, Town", result.address);
            assertEquals("+1-555-987-6543", result.phone);
            assertEquals("MX", result.countryCode);
            assertEquals("Mexican", result.demonym);
            assertNotNull(result.updatedAt);
        }

        @Test
        @DisplayName("throws 404 for non-existent client")
        void shouldThrowExceptionWhenUpdatingNonExistentClient() {
            String nonExistentUuid = "00000000-0000-0000-0000-000000000000";
            when(clientRepository.findActiveByUuid(nonExistentUuid)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> clientService.updateClient(nonExistentUuid, validUpdateRequest));
        }

        @Test
        @DisplayName("rejects email that belongs to another client")
        void shouldThrowExceptionWhenEmailBelongsToAnother() {
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(true);

            assertThrows(DuplicateEmailException.class,
                    () -> clientService.updateClient(existingClient.uuid, validUpdateRequest));
        }

        @Test
        @DisplayName("rejects phone that belongs to another client")
        void shouldThrowExceptionWhenPhoneBelongsToAnother() {
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(clientRepository.existsByPhoneAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(true);

            assertThrows(DuplicatePhoneException.class,
                    () -> clientService.updateClient(existingClient.uuid, validUpdateRequest));
        }

        @Test
        @DisplayName("allows keeping the same email")
        void shouldAllowUpdatingToSameEmail() {
            validUpdateRequest.email = existingClient.email;
            validUpdateRequest.countryCode = existingClient.countryCode;
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(clientRepository.existsByPhoneAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);

            Client result = clientService.updateClient(existingClient.uuid, validUpdateRequest);

            assertEquals(existingClient.email, result.email);
        }

        @Test
        @DisplayName("rejects invalid country code on update")
        void shouldThrowExceptionForInvalidCountryCodeOnUpdate() {
            validUpdateRequest.countryCode = "RD";
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(clientRepository.existsByPhoneAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(countryService.getDemonym("RD")).thenThrow(new InvalidCountryCodeException("RD"));

            assertThrows(InvalidCountryCodeException.class,
                    () -> clientService.updateClient(existingClient.uuid, validUpdateRequest));

            // Original country code should remain unchanged
            assertEquals("US", existingClient.countryCode);
        }

        @Test
        @DisplayName("skips demonym fetch if country unchanged")
        void shouldNotFetchDemonymIfCountryUnchanged() {
            validUpdateRequest.countryCode = "US";
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);
            when(clientRepository.existsByPhoneAndUuidNot(anyString(), eq(existingClient.uuid))).thenReturn(false);

            clientService.updateClient(existingClient.uuid, validUpdateRequest);

            verify(countryService, never()).getDemonym(anyString());
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class DeleteClientTests {

        @Test
        @DisplayName("deactivates client instead of removing")
        void shouldSoftDeleteClientSuccessfully() {
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.of(existingClient));

            clientService.deleteClient(existingClient.uuid);

            assertFalse(existingClient.active);
            assertNotNull(existingClient.updatedAt);
            verify(clientRepository, never()).delete(any(Client.class));
        }

        @Test
        @DisplayName("throws 404 for non-existent client")
        void shouldThrowExceptionWhenDeletingNonExistentClient() {
            String nonExistentUuid = "00000000-0000-0000-0000-000000000000";
            when(clientRepository.findActiveByUuid(nonExistentUuid)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> clientService.deleteClient(nonExistentUuid));
        }

        @Test
        @DisplayName("soft-deleted client is not findable")
        void shouldNotFindSoftDeletedClient() {
            when(clientRepository.findActiveByUuid(existingClient.uuid)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> clientService.getClientByUuid(existingClient.uuid));
        }
    }

    @Nested
    @DisplayName("Count")
    class CountClientTests {

        @Test
        @DisplayName("counts only active clients")
        void shouldCountAllClients() {
            when(clientRepository.countActive()).thenReturn(5L);

            long result = clientService.countClients();

            assertEquals(5L, result);
        }

        @Test
        @DisplayName("counts active clients by country")
        void shouldCountClientsByCountry() {
            when(clientRepository.countActiveByCountryCode("US")).thenReturn(3L);

            long result = clientService.countClientsByCountry("US");

            assertEquals(3L, result);
        }
    }
}
