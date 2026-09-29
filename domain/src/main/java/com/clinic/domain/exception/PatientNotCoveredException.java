package com.clinic.domain.exception;

import com.clinic.domain.model.PatientId;

public class PatientNotCoveredException extends DomainException {
    public PatientNotCoveredException(PatientId id) {
        super("Patient " + id + " has no active health coverage");
    }
}
