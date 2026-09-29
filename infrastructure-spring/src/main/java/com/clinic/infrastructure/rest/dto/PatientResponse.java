package com.clinic.infrastructure.rest.dto;

import com.clinic.domain.model.Patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(UUID id, String fullName, String documentId, LocalDate birthDate) {

    public static PatientResponse from(Patient p) {
        return new PatientResponse(p.id().value(), p.fullName(), p.documentId(), p.birthDate());
    }
}
