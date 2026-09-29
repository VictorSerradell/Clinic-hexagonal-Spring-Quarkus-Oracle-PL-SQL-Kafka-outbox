package com.clinic.domain.event;

import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.PatientId;
import com.clinic.domain.model.TimeSlot;

import java.time.Instant;

public record AppointmentScheduled(AppointmentId appointmentId, PatientId patientId, DoctorId doctorId,
                                   TimeSlot slot, Instant occurredAt) implements DomainEvent {
}
