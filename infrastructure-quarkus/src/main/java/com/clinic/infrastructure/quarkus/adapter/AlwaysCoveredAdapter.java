package com.clinic.infrastructure.quarkus.adapter;

import com.clinic.application.port.out.CoverageVerificationPort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlwaysCoveredAdapter implements CoverageVerificationPort {

    @Override
    public boolean hasActiveCoverage(String documentId) {
        return true;
    }
}
