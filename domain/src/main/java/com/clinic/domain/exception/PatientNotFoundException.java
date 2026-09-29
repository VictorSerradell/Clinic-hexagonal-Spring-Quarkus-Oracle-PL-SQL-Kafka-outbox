package com.clinic.domain.exception;

import com.clinic.domain.model.PatientId;

public class PatientNotFoundException extends DomainException {
    public PatientNotFoundException(PatientId id) {
        super("Patient " + id + " not found");
    }
}
