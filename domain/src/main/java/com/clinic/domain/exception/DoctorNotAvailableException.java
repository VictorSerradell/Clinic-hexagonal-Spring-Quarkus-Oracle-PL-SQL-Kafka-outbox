package com.clinic.domain.exception;

import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.TimeSlot;

public class DoctorNotAvailableException extends DomainException {
    public DoctorNotAvailableException(DoctorId id, TimeSlot slot) {
        super("Doctor " + id + " is not available between " + slot.start() + " and " + slot.end());
    }
}
