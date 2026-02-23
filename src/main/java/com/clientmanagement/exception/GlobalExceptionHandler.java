package com.clientmanagement.exception;

import com.clientmanagement.dto.common.ApiResponse;
import com.clientmanagement.dto.common.FieldError;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ClientNotFoundException e) {
            LOG.warn("Client not found: " + e.getMessage());
            return buildErrorResponse(Response.Status.NOT_FOUND, e.getMessage());
        }

        if (exception instanceof DuplicateEmailException e) {
            LOG.warn("Duplicate email: " + e.getMessage());
            return buildErrorResponse(Response.Status.CONFLICT, e.getMessage());
        }

        if (exception instanceof DuplicatePhoneException e) {
            LOG.warn("Duplicate phone: " + e.getMessage());
            return buildErrorResponse(Response.Status.CONFLICT, e.getMessage());
        }

        if (exception instanceof BadRequestException e) {
            LOG.warn("Bad request: " + e.getMessage());
            return buildErrorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }

        if (exception instanceof InvalidCountryCodeException e) {
            LOG.warn("Invalid country code: " + e.getMessage());
            return buildErrorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }

        if (exception instanceof CountryServiceException e) {
            LOG.error("Country service error: " + e.getMessage(), e);
            return buildErrorResponse(Response.Status.SERVICE_UNAVAILABLE,
                    "Unable to fetch country information. Please try again later.");
        }

        if (exception instanceof ConstraintViolationException e) {
            LOG.warn("Validation error: " + e.getMessage());
            List<FieldError> fieldErrors = e.getConstraintViolations().stream()
                    .map(this::toFieldError)
                    .collect(Collectors.toList());
            ApiResponse<Void> response = ApiResponse.validationError(
                    "Validation failed for one or more fields", fieldErrors);
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        if (exception instanceof PersistenceException e) {
            String message = extractConstraintMessage(e);
            LOG.warn("Database constraint violation: " + message);
            return buildErrorResponse(Response.Status.CONFLICT, message);
        }

        LOG.error("Unexpected error: " + exception.getMessage(), exception);
        return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    private FieldError toFieldError(ConstraintViolation<?> violation) {
        String field = getFieldName(violation.getPropertyPath().toString());
        return new FieldError(field, violation.getMessage(), violation.getInvalidValue());
    }

    private String getFieldName(String propertyPath) {
        if (propertyPath.contains(".")) {
            String[] parts = propertyPath.split("\\.");
            return parts[parts.length - 1];
        }
        return propertyPath;
    }

    private String extractConstraintMessage(PersistenceException exception) {
        String rootMessage = getRootCauseMessage(exception).toLowerCase();
        if (rootMessage.contains("uk_client_email") || rootMessage.contains("email")) {
            return "A client with this email already exists";
        }
        if (rootMessage.contains("uk_client_phone") || rootMessage.contains("phone")) {
            return "A client with this phone number already exists";
        }
        if (rootMessage.contains("uuid")) {
            return "A client with this identifier already exists";
        }
        return "A record with the provided data already exists";
    }

    private String getRootCauseMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : "";
    }

    private Response buildErrorResponse(Response.Status status, String message) {
        ApiResponse<Void> response = ApiResponse.error(message);
        return Response.status(status).entity(response).build();
    }
}
