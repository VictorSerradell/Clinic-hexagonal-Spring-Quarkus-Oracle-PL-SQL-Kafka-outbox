package com.clinic.infrastructure.rest.dto;

import com.clinic.domain.model.Appointment;

import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(UUID id, UUID patientId, UUID doctorId, Instant startsAt, Instant endsAt,
                                  String reason, String status) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.id().value(), a.patientId().value(), a.doctorId().value(),
                a.slot().start(), a.slot().end(), a.reason(), a.status().name());
    }
}
