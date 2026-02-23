package com.clientmanagement.exception;

import com.clientmanagement.dto.common.ErrorResponse;
import com.clientmanagement.dto.common.FieldError;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles JSON parsing errors so the client gets a readable message
 * instead of a raw stack trace.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger LOG = Logger.getLogger(ValidationExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(JsonMappingException exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        String fieldPath = extractFieldPath(exception);
        String message;

        if (exception instanceof InvalidFormatException ife) {
            message = String.format("Invalid value for field '%s': expected %s but received '%s'",
                    fieldPath, ife.getTargetType().getSimpleName(), ife.getValue());
        } else {
            message = "Invalid JSON format: " + exception.getOriginalMessage();
        }

        LOG.warn("JSON mapping error: " + message);

        List<FieldError> fieldErrors = List.of(
                new FieldError(fieldPath, message, null)
        );

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                "Invalid request body",
                path,
                fieldErrors
        );

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

    private String extractFieldPath(JsonMappingException exception) {
        return exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(name -> name != null)
                .collect(Collectors.joining("."));
    }
}
