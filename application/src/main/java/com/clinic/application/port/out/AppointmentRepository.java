package com.clinic.application.port.out;

import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.PatientId;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(AppointmentId id);

    List<Appointment> findByPatientId(PatientId patientId);
}
