package com.clinic.domain.model;

import com.clinic.domain.exception.InvalidDataException;

import java.time.LocalDate;
import java.util.Objects;

public record Patient(PatientId id, String fullName, String documentId, LocalDate birthDate) {

    public Patient {
        Objects.requireNonNull(id, "id");
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidDataException("Full name is required");
        }
        if (documentId == null || documentId.isBlank()) {
            throw new InvalidDataException("Document id is required");
        }
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
            throw new InvalidDataException("Birth date must be in the past");
        }
        fullName = fullName.trim();
        documentId = documentId.trim().toUpperCase();
    }

    public static Patient register(String fullName, String documentId, LocalDate birthDate) {
        return new Patient(PatientId.newId(), fullName, documentId, birthDate);
    }
}
