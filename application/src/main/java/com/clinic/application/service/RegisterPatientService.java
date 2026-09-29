package com.clinic.application.service;

import com.clinic.application.port.in.RegisterPatientUseCase;
import com.clinic.application.port.out.PatientRepository;
import com.clinic.domain.exception.DuplicatePatientException;
import com.clinic.domain.model.Patient;

public class RegisterPatientService implements RegisterPatientUseCase {

    private final PatientRepository patients;

    public RegisterPatientService(PatientRepository patients) {
        this.patients = patients;
    }

    @Override
    public Patient register(Command command) {
        Patient patient = Patient.register(command.fullName(), command.documentId(), command.birthDate());
        patients.findByDocumentId(patient.documentId()).ifPresent(existing -> {
            throw new DuplicatePatientException(patient.documentId());
        });
        return patients.save(patient);
    }
}
