package com.clinic.domain.exception;

public class DuplicatePatientException extends DomainException {
    public DuplicatePatientException(String documentId) {
        super("A patient with document id " + documentId + " already exists");
    }
}
