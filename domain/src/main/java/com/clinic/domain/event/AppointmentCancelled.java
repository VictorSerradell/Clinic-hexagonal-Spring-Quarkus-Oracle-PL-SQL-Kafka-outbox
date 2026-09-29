package com.clinic.domain.event;

import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.PatientId;

import java.time.Instant;

public record AppointmentCancelled(AppointmentId appointmentId, PatientId patientId,
                                   Instant occurredAt) implements DomainEvent {
}
