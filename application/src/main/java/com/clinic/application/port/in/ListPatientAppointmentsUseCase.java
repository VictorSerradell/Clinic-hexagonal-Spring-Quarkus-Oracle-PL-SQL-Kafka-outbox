package com.clinic.application.port.in;

import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.PatientId;

import java.util.List;

public interface ListPatientAppointmentsUseCase {

    List<Appointment> list(PatientId patientId);
}
