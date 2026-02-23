package com.clientmanagement.client;

import com.clientmanagement.dto.country.CountryData;
import com.clientmanagement.exception.CountryServiceException;
import com.clientmanagement.exception.InvalidCountryCodeException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Wraps the RestCountries external API to fetch demonyms and validate country codes.
 */
@ApplicationScoped
public class CountryService {

    private static final Logger LOG = Logger.getLogger(CountryService.class);

    @Inject
    @RestClient
    RestCountriesClient restCountriesClient;

    /**
     * Fetches the demonym for a country code. Throws if the code doesn't exist.
     */
    public String getDemonym(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }

        String code = countryCode.toUpperCase().trim();
        LOG.debug("Fetching demonym for country code: " + code);

        try {
            List<CountryData> countries = restCountriesClient.getCountryByCode(code);
            
            if (countries == null || countries.isEmpty()) {
                LOG.warn("No country data found for code: " + code);
                throw new InvalidCountryCodeException(code);
            }

            CountryData country = countries.get(0);
            String demonym = extractDemonym(country);
            
            LOG.info("Successfully fetched demonym for " + code + ": " + demonym);
            return demonym;

        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                LOG.warn("Country not found for code: " + code);
                throw new InvalidCountryCodeException(code);
            }
            LOG.error("Error fetching country data: " + e.getMessage(), e);
            throw new CountryServiceException(code, e);
        } catch (InvalidCountryCodeException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error fetching country data: " + e.getMessage(), e);
            throw new CountryServiceException(code, e);
        }
    }

    private String extractDemonym(CountryData country) {
        if (country.demonyms != null) {
            if (country.demonyms.eng != null && country.demonyms.eng.m != null) {
                return country.demonyms.eng.m;
            }
            if (country.demonyms.spa != null && country.demonyms.spa.m != null) {
                return country.demonyms.spa.m;
            }
        }
        // Fallback to country name if demonym isn't available
        if (country.name != null && country.name.common != null) {
            return country.name.common;
        }
        return null;
    }

    public boolean isValidCountryCode(String countryCode) {
        try {
            getDemonym(countryCode);
            return true;
        } catch (InvalidCountryCodeException e) {
            return false;
        } catch (CountryServiceException e) {
            // If the service is down, we let the code through for now
            LOG.warn("Could not validate country code due to service error: " + e.getMessage());
            return true;
        }
    }

    public Optional<String> getCountryName(String countryCode) {
        try {
            List<CountryData> countries = restCountriesClient.getCountryByCode(countryCode);
            if (countries != null && !countries.isEmpty() && countries.get(0).name != null) {
                return Optional.ofNullable(countries.get(0).name.common);
            }
        } catch (Exception e) {
            LOG.warn("Could not fetch country name: " + e.getMessage());
        }
        return Optional.empty();
    }
}
