package com.clinic.application.port.in;

import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.PatientId;

import java.time.Instant;

public interface ScheduleAppointmentUseCase {

    Appointment schedule(Command command);

    record Command(PatientId patientId, DoctorId doctorId, Instant startsAt, Instant endsAt, String reason) {
    }
}
