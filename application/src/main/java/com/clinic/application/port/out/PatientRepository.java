package com.clinic.application.port.out;

import com.clinic.domain.model.Patient;
import com.clinic.domain.model.PatientId;

import java.util.Optional;

public interface PatientRepository {

    Patient save(Patient patient);

    Optional<Patient> findById(PatientId id);

    Optional<Patient> findByDocumentId(String documentId);
}
