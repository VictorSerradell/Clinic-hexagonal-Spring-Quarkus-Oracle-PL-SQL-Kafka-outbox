package com.clinic.infrastructure.coverage;

import com.clinic.application.port.out.CoverageVerificationPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Consumes an external REST API: GET {base-url}/coverages/{documentId} -> {"active": true}. */
public class RestCoverageVerificationAdapter implements CoverageVerificationPort {

    private final RestClient client;

    public RestCoverageVerificationAdapter(RestClient client) {
        this.client = client;
    }

    @Override
    public boolean hasActiveCoverage(String documentId) {
        try {
            CoverageResponse response = client.get()
                    .uri("/coverages/{documentId}", documentId)
                    .retrieve()
                    .body(CoverageResponse.class);
            return response != null && response.active();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new CoverageUnavailableException(e);
        }
    }

    record CoverageResponse(boolean active) {
    }
}
