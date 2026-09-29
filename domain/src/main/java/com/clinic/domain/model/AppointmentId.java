package com.clinic.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AppointmentId(UUID value) {

    public AppointmentId {
        Objects.requireNonNull(value, "value");
    }

    public static AppointmentId newId() {
        return new AppointmentId(UUID.randomUUID());
    }

    public static AppointmentId of(String value) {
        return new AppointmentId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
