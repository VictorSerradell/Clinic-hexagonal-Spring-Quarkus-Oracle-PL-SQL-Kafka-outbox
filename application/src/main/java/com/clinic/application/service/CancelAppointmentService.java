package com.clinic.application.service;

import com.clinic.application.port.in.CancelAppointmentUseCase;
import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.domain.event.AppointmentCancelled;
import com.clinic.domain.exception.AppointmentNotFoundException;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;

import java.time.Clock;

public class CancelAppointmentService implements CancelAppointmentUseCase {

    private final AppointmentRepository appointments;
    private final DomainEventPublisher events;
    private final Clock clock;

    public CancelAppointmentService(AppointmentRepository appointments, DomainEventPublisher events, Clock clock) {
        this.appointments = appointments;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public Appointment cancel(AppointmentId appointmentId) {
        Appointment appointment = appointments.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.cancel(clock.instant());
        Appointment saved = appointments.save(appointment);

        events.publish(new AppointmentCancelled(saved.id(), saved.patientId(), clock.instant()));
        return saved;
    }
}
