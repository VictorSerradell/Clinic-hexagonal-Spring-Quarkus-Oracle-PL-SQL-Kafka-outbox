package com.clinic.application.service;

import com.clinic.application.port.in.ListPatientAppointmentsUseCase;
import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.application.port.out.PatientRepository;
import com.clinic.domain.exception.PatientNotFoundException;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.PatientId;

import java.util.List;

public class ListPatientAppointmentsService implements ListPatientAppointmentsUseCase {

    private final PatientRepository patients;
    private final AppointmentRepository appointments;

    public ListPatientAppointmentsService(PatientRepository patients, AppointmentRepository appointments) {
        this.patients = patients;
        this.appointments = appointments;
    }

    @Override
    public List<Appointment> list(PatientId patientId) {
        patients.findById(patientId).orElseThrow(() -> new PatientNotFoundException(patientId));
        return appointments.findByPatientId(patientId);
    }
}
