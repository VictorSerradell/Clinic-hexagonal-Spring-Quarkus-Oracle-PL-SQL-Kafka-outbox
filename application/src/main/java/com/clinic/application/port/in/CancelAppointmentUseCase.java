package com.clinic.application.port.in;

import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;

public interface CancelAppointmentUseCase {

    Appointment cancel(AppointmentId appointmentId);
}
