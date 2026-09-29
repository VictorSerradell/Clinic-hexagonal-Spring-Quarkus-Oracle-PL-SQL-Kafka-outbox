package com.clinic.domain.model;

import com.clinic.domain.exception.AppointmentStateException;
import com.clinic.domain.exception.InvalidDataException;

import java.time.Instant;
import java.util.Objects;

/** Aggregate root. All business invariants of an appointment live here. */
public final class Appointment {

    private final AppointmentId id;
    private final PatientId patientId;
    private final DoctorId doctorId;
    private final TimeSlot slot;
    private final String reason;
    private AppointmentStatus status;

    private Appointment(AppointmentId id, PatientId patientId, DoctorId doctorId,
                        TimeSlot slot, String reason, AppointmentStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.patientId = Objects.requireNonNull(patientId, "patientId");
        this.doctorId = Objects.requireNonNull(doctorId, "doctorId");
        this.slot = Objects.requireNonNull(slot, "slot");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.status = Objects.requireNonNull(status, "status");
    }

    public static Appointment schedule(PatientId patientId, DoctorId doctorId, TimeSlot slot,
                                       String reason, Instant now) {
        if (!slot.start().isAfter(now)) {
            throw new InvalidDataException("An appointment must start in the future");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidDataException("The reason for the appointment is required");
        }
        return new Appointment(AppointmentId.newId(), patientId, doctorId, slot,
                reason.trim(), AppointmentStatus.SCHEDULED);
    }

    /** Rebuilds an aggregate from persisted state (no validation of "future" rules). */
    public static Appointment restore(AppointmentId id, PatientId patientId, DoctorId doctorId,
                                      TimeSlot slot, String reason, AppointmentStatus status) {
        return new Appointment(id, patientId, doctorId, slot, reason, status);
    }

    public void cancel(Instant now) {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new AppointmentStateException("Only scheduled appointments can be cancelled (current: " + status + ")");
        }
        if (!slot.start().isAfter(now)) {
            throw new AppointmentStateException("An appointment that has already started cannot be cancelled");
        }
        this.status = AppointmentStatus.CANCELLED;
    }

    public AppointmentId id() { return id; }
    public PatientId patientId() { return patientId; }
    public DoctorId doctorId() { return doctorId; }
    public TimeSlot slot() { return slot; }
    public String reason() { return reason; }
    public AppointmentStatus status() { return status; }
}
