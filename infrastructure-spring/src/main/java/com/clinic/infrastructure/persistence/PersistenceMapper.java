package com.clinic.infrastructure.persistence;

import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.AppointmentStatus;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.Patient;
import com.clinic.domain.model.PatientId;
import com.clinic.domain.model.TimeSlot;

/** Translates between domain objects and JPA entities so the domain never sees persistence annotations. */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static PatientJpaEntity toEntity(Patient p) {
        return new PatientJpaEntity(p.id().toString(), p.fullName(), p.documentId(), p.birthDate());
    }

    static Patient toDomain(PatientJpaEntity e) {
        return new Patient(PatientId.of(e.getId()), e.getFullName(), e.getDocumentId(), e.getBirthDate());
    }

    static AppointmentJpaEntity toEntity(Appointment a) {
        return new AppointmentJpaEntity(a.id().toString(), a.patientId().toString(), a.doctorId().toString(),
                a.slot().start(), a.slot().end(), a.reason(), a.status().name());
    }

    static Appointment toDomain(AppointmentJpaEntity e) {
        return Appointment.restore(AppointmentId.of(e.getId()), PatientId.of(e.getPatientId()),
                DoctorId.of(e.getDoctorId()), new TimeSlot(e.getStartsAt(), e.getEndsAt()),
                e.getReason(), AppointmentStatus.valueOf(e.getStatus()));
    }
}
