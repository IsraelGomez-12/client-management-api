package com.clientmanagement.resource;

import com.clientmanagement.dto.client.ClientResponse;
import com.clientmanagement.dto.client.CreateClientRequest;
import com.clientmanagement.dto.client.UpdateClientRequest;
import com.clientmanagement.dto.common.ApiResponse;
import com.clientmanagement.entity.Client;
import com.clientmanagement.service.ClientService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Clients", description = "Client management operations")
public class ClientResource {

    private static final Logger LOG = Logger.getLogger(ClientResource.class);

    @Inject
    ClientService clientService;

    @Context
    UriInfo uriInfo;

    @POST
    @Operation(summary = "Create a new client", description = "Creates a new client with the provided information. The demonym is automatically fetched based on the country code.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Client created successfully",
                content = @Content(schema = @Schema(implementation = ClientResponse.class))),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "409", description = "Email already exists")
    })
    public Response createClient(@Valid CreateClientRequest request) {
        LOG.info("Received request to create new client");

        Client client = clientService.createClient(request);
        ClientResponse clientResponse = ClientResponse.fromEntity(client);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(client.uuid)
                .build();

        ApiResponse<ClientResponse> response = ApiResponse.created(clientResponse, "Client created successfully");
        return Response.created(location).entity(response).build();
    }

    @GET
    @Operation(summary = "Get all clients", description = "Retrieves all active clients ordered by creation date")
    @APIResponse(responseCode = "200", description = "Clients retrieved successfully",
            content = @Content(schema = @Schema(implementation = ClientResponse.class, type = SchemaType.ARRAY)))
    public Response getAllClients() {
        LOG.info("Received request to get all clients");

        List<ClientResponse> clients = clientService.getAllClients().stream()
                .map(ClientResponse::fromEntity)
                .collect(Collectors.toList());

        ApiResponse<List<ClientResponse>> response = ApiResponse.ok(clients, "Clients retrieved successfully");
        return Response.ok(response).build();
    }

    @GET
    @Path("/country/{countryCode}")
    @Operation(summary = "Get clients by country", description = "Retrieves all clients from a specific country using the ISO 3166-1 alpha-2 country code")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Clients retrieved successfully",
                content = @Content(schema = @Schema(implementation = ClientResponse.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "400", description = "Invalid country code format")
    })
    public Response getClientsByCountry(
            @Parameter(description = "ISO 3166-1 alpha-2 country code (e.g., US, MX, ES)", required = true)
            @PathParam("countryCode") String countryCode) {
        LOG.info("Received request to get clients from country: " + countryCode);

        if (countryCode == null || !countryCode.matches("^[A-Za-z]{2}$")) {
            throw new BadRequestException("Country code must be a valid ISO 3166-1 alpha-2 code (2 letters)");
        }

        List<ClientResponse> clients = clientService.getClientsByCountry(countryCode).stream()
                .map(ClientResponse::fromEntity)
                .collect(Collectors.toList());

        ApiResponse<List<ClientResponse>> response = ApiResponse.ok(clients, "Clients retrieved successfully");
        return Response.ok(response).build();
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Get client by ID", description = "Retrieves a specific client by their UUID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Client retrieved successfully",
                content = @Content(schema = @Schema(implementation = ClientResponse.class))),
        @APIResponse(responseCode = "404", description = "Client not found")
    })
    public Response getClientByUuid(
            @Parameter(description = "Client UUID", required = true)
            @PathParam("uuid") String uuid) {
        LOG.info("Received request to get client with uuid: " + uuid);

        Client client = clientService.getClientByUuid(uuid);
        ClientResponse clientResponse = ClientResponse.fromEntity(client);

        ApiResponse<ClientResponse> response = ApiResponse.ok(clientResponse, "Client retrieved successfully");
        return Response.ok(response).build();
    }

    @PATCH
    @Path("/{uuid}")
    @Operation(summary = "Update client", description = "Partially updates an existing client. Only email, address, phone, and country code can be modified.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Client updated successfully",
                content = @Content(schema = @Schema(implementation = ClientResponse.class))),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "404", description = "Client not found"),
        @APIResponse(responseCode = "409", description = "Email already exists")
    })
    public Response updateClient(
            @Parameter(description = "Client UUID", required = true)
            @PathParam("uuid") String uuid,
            @Valid UpdateClientRequest request) {
        LOG.info("Received request to update client with uuid: " + uuid);

        Client client = clientService.updateClient(uuid, request);
        ClientResponse clientResponse = ClientResponse.fromEntity(client);

        ApiResponse<ClientResponse> response = ApiResponse.ok(clientResponse, "Client updated successfully");
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Delete client", description = "Soft-deletes a client by UUID. The record is deactivated but stays in the database.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Client deactivated successfully"),
        @APIResponse(responseCode = "404", description = "Client not found")
    })
    public Response deleteClient(
            @Parameter(description = "Client UUID", required = true)
            @PathParam("uuid") String uuid) {
        LOG.info("Received request to delete client with uuid: " + uuid);

        clientService.deleteClient(uuid);

        ApiResponse<Void> response = ApiResponse.noContent("Client deleted successfully");
        return Response.ok(response).build();
    }

    @GET
    @Path("/count")
    @Operation(summary = "Get client count", description = "Returns the total number of active clients")
    @APIResponse(responseCode = "200", description = "Count retrieved successfully")
    public Response getClientCount() {
        long count = clientService.countClients();
        ApiResponse<Long> response = ApiResponse.ok(count, "Client count retrieved successfully");
        return Response.ok(response).build();
    }
}
