package com.clientmanagement.resource;

import com.clientmanagement.client.CountryService;
import com.clientmanagement.entity.Client;
import com.clientmanagement.repository.ClientRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

@QuarkusTest
class ClientResourceTest {

    private static final String BASE_PATH = "/api/v1/clients";

    @InjectMock
    ClientRepository clientRepository;

    @InjectMock
    CountryService countryService;

    private Client testClient;

    private static final String TEST_UUID = "7b2a4e8f-3c1d-4a5b-9e6f-8d7c2b1a0e3f";
    private static final String NON_EXISTENT_UUID = "00000000-0000-0000-0000-000000000000";

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.id = 1L;
        testClient.uuid = TEST_UUID;
        testClient.firstName = "John";
        testClient.secondName = "Michael";
        testClient.firstSurname = "Doe";
        testClient.secondSurname = "Smith";
        testClient.email = "john.doe@example.com";
        testClient.address = "123 Main Street, City";
        testClient.phone = "+1-555-123-4567";
        testClient.countryCode = "US";
        testClient.demonym = "American";
        testClient.active = true;
        testClient.createdAt = LocalDateTime.now();
        testClient.updatedAt = LocalDateTime.now();
    }

    @Nested
    @DisplayName("POST /api/v1/clients")
    class CreateClientEndpoint {

        @Test
        @DisplayName("201 - valid client")
        void shouldCreateClient() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(false);
            when(countryService.getDemonym("US")).thenReturn("American");
            doAnswer(invocation -> {
                Client c = invocation.getArgument(0);
                c.uuid = TEST_UUID;
                return null;
            }).when(clientRepository).persist(any(Client.class));
            doNothing().when(clientRepository).flush();

            String requestBody = """
                {
                    "firstName": "John",
                    "secondName": "Michael",
                    "firstSurname": "Doe",
                    "secondSurname": "Smith",
                    "email": "john.doe@example.com",
                    "address": "123 Main Street, City",
                    "phone": "+1-555-123-4567",
                    "countryCode": "US"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("message", equalTo("Client created successfully"))
                .body("data.firstName", equalTo("John"))
                .body("data.email", equalTo("john.doe@example.com"))
                .body("data.countryCode", equalTo("US"));
        }

        @Test
        @DisplayName("400 - missing required fields")
        void shouldReturn400ForMissingFields() {
            String requestBody = """
                {
                    "firstName": "John"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("400 - bad email format")
        void shouldReturn400ForInvalidEmail() {
            String requestBody = """
                {
                    "firstName": "John",
                    "firstSurname": "Doe",
                    "email": "invalid-email",
                    "address": "123 Main Street",
                    "phone": "+1-555-123-4567",
                    "countryCode": "US"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("400 - invalid country code format")
        void shouldReturn400ForInvalidCountryCode() {
            String requestBody = """
                {
                    "firstName": "John",
                    "firstSurname": "Doe",
                    "email": "john@example.com",
                    "address": "123 Main Street",
                    "phone": "+1-555-123-4567",
                    "countryCode": "USA"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("409 - duplicate email")
        void shouldReturn409ForDuplicateEmail() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(true);

            String requestBody = """
                {
                    "firstName": "John",
                    "firstSurname": "Doe",
                    "email": "existing@example.com",
                    "address": "123 Main Street",
                    "phone": "+1-555-123-4567",
                    "countryCode": "US"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(409)
                .body("success", equalTo(false));
        }

        @Test
        @DisplayName("409 - duplicate phone")
        void shouldReturn409ForDuplicatePhone() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(true);

            String requestBody = """
                {
                    "firstName": "John",
                    "firstSurname": "Doe",
                    "email": "new@example.com",
                    "address": "123 Main Street",
                    "phone": "+1-555-123-4567",
                    "countryCode": "US"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(409)
                .body("success", equalTo(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clients")
    class GetAllClientsEndpoint {

        @Test
        @DisplayName("200 - returns client list")
        void shouldReturnAllClients() {
            when(clientRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(testClient));

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", hasSize(1))
                .body("data[0].firstName", equalTo("John"))
                .body("data[0].email", equalTo("john.doe@example.com"));
        }

        @Test
        @DisplayName("200 - empty list")
        void shouldReturnEmptyList() {
            when(clientRepository.findAllOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clients/{uuid}")
    class GetClientByUuidEndpoint {

        @Test
        @DisplayName("200 - found")
        void shouldReturnClient() {
            when(clientRepository.findActiveByUuid(TEST_UUID)).thenReturn(Optional.of(testClient));

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH + "/" + TEST_UUID)
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(TEST_UUID))
                .body("data.firstName", equalTo("John"))
                .body("data.fullName", containsString("John"));
        }

        @Test
        @DisplayName("404 - not found")
        void shouldReturn404ForNonExistentClient() {
            when(clientRepository.findActiveByUuid(NON_EXISTENT_UUID)).thenReturn(Optional.empty());

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH + "/" + NON_EXISTENT_UUID)
            .then()
                .statusCode(404)
                .body("success", equalTo(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clients/country/{code}")
    class GetClientsByCountryEndpoint {

        @Test
        @DisplayName("200 - filters by country")
        void shouldReturnClientsByCountry() {
            when(clientRepository.findByCountryCode("US")).thenReturn(List.of(testClient));

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH + "/country/US")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", hasSize(1))
                .body("data[0].countryCode", equalTo("US"));
        }

        @Test
        @DisplayName("200 - handles lowercase input")
        void shouldHandleLowercaseCountryCode() {
            when(clientRepository.findByCountryCode("US")).thenReturn(List.of(testClient));

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH + "/country/us")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("400 - rejects 3-letter code")
        void shouldReturn400ForInvalidCountryCodeFormat() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH + "/country/USA")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/clients/{uuid}")
    class UpdateClientEndpoint {

        @Test
        @DisplayName("200 - updates successfully")
        void shouldUpdateClient() {
            when(clientRepository.findActiveByUuid(TEST_UUID)).thenReturn(Optional.of(testClient));
            when(clientRepository.existsByEmailAndUuidNot(anyString(), eq(TEST_UUID))).thenReturn(false);
            when(clientRepository.existsByPhoneAndUuidNot(anyString(), eq(TEST_UUID))).thenReturn(false);
            when(countryService.getDemonym("MX")).thenReturn("Mexican");

            String requestBody = """
                {
                    "email": "john.updated@example.com",
                    "address": "456 New Avenue",
                    "phone": "+1-555-987-6543",
                    "countryCode": "MX"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .patch(BASE_PATH + "/" + TEST_UUID)
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.email", equalTo("john.updated@example.com"))
                .body("data.address", equalTo("456 New Avenue"));
        }

        @Test
        @DisplayName("404 - client not found")
        void shouldReturn404ForNonExistentClient() {
            when(clientRepository.findActiveByUuid(NON_EXISTENT_UUID)).thenReturn(Optional.empty());

            String requestBody = """
                {
                    "email": "john@example.com",
                    "address": "456 New Avenue",
                    "phone": "+1-555-987-6543",
                    "countryCode": "US"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .patch(BASE_PATH + "/" + NON_EXISTENT_UUID)
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("400 - invalid update data")
        void shouldReturn400ForInvalidData() {
            String requestBody = """
                {
                    "email": "invalid-email",
                    "address": "123",
                    "phone": "abc",
                    "countryCode": "USA"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .patch(BASE_PATH + "/" + TEST_UUID)
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/clients/{uuid}")
    class DeleteClientEndpoint {

        @Test
        @DisplayName("200 - soft deletes")
        void shouldDeleteClient() {
            when(clientRepository.findActiveByUuid(TEST_UUID)).thenReturn(Optional.of(testClient));

            given()
                .contentType(ContentType.JSON)
            .when()
                .delete(BASE_PATH + "/" + TEST_UUID)
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Client deleted successfully"));
        }

        @Test
        @DisplayName("404 - not found")
        void shouldReturn404ForNonExistentClient() {
            when(clientRepository.findActiveByUuid(NON_EXISTENT_UUID)).thenReturn(Optional.empty());

            given()
                .contentType(ContentType.JSON)
            .when()
                .delete(BASE_PATH + "/" + NON_EXISTENT_UUID)
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clients/count")
    class GetClientCountEndpoint {

        @Test
        @DisplayName("200 - returns count")
        void shouldReturnClientCount() {
            when(clientRepository.countActive()).thenReturn(5L);

            given()
                .contentType(ContentType.JSON)
            .when()
                .get(BASE_PATH + "/count")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", equalTo(5));
        }
    }
}
