package com.clinic.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "appointments")
public class AppointmentJpaEntity {

    @Id
    private String id;
    private String patientId;
    private String doctorId;
    private Instant startsAt;
    private Instant endsAt;
    private String reason;
    private String status;

    protected AppointmentJpaEntity() {
    }

    public AppointmentJpaEntity(String id, String patientId, String doctorId,
                                Instant startsAt, Instant endsAt, String reason, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.reason = reason;
        this.status = status;
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
}
