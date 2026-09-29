package com.clinic.domain.exception;

import com.clinic.domain.model.AppointmentId;

public class AppointmentNotFoundException extends DomainException {
    public AppointmentNotFoundException(AppointmentId id) {
        super("Appointment " + id + " not found");
    }
}
