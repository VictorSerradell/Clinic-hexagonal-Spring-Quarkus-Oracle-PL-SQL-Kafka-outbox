package com.clinic.application.port.out;

import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.TimeSlot;

/** Outbound port: "is this doctor free in this slot?". Implemented in Oracle with PL/SQL in the Spring adapter. */
public interface DoctorAvailabilityPort {

    boolean isAvailable(DoctorId doctorId, TimeSlot slot);
}
