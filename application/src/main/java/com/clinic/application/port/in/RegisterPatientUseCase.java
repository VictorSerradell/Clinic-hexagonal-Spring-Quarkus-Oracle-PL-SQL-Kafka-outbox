package com.clinic.application.port.in;

import com.clinic.domain.model.Patient;

import java.time.LocalDate;

public interface RegisterPatientUseCase {

    Patient register(Command command);

    record Command(String fullName, String documentId, LocalDate birthDate) {
    }
}
