package com.clientmanagement.client;

import com.clientmanagement.dto.country.CountryData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * MicroProfile REST client for https://restcountries.com
 */
@RegisterRestClient(configKey = "restcountries-api")
@Path("/alpha")
public interface RestCountriesClient {

    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    List<CountryData> getCountryByCode(@PathParam("code") String code);
}
