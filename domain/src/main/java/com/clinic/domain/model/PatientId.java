package com.clinic.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PatientId(UUID value) {

    public PatientId {
        Objects.requireNonNull(value, "value");
    }

    public static PatientId newId() {
        return new PatientId(UUID.randomUUID());
    }

    public static PatientId of(String value) {
        return new PatientId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
