package com.clinic.domain.model;

import java.util.Objects;
import java.util.UUID;

public record DoctorId(UUID value) {

    public DoctorId {
        Objects.requireNonNull(value, "value");
    }

    public static DoctorId newId() {
        return new DoctorId(UUID.randomUUID());
    }

    public static DoctorId of(String value) {
        return new DoctorId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
