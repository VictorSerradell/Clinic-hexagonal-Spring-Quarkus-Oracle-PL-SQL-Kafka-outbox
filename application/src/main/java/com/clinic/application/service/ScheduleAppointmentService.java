package com.clinic.application.service;

import com.clinic.application.port.in.ScheduleAppointmentUseCase;
import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.application.port.out.CoverageVerificationPort;
import com.clinic.application.port.out.DoctorAvailabilityPort;
import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.application.port.out.PatientRepository;
import com.clinic.domain.event.AppointmentScheduled;
import com.clinic.domain.exception.DoctorNotAvailableException;
import com.clinic.domain.exception.PatientNotCoveredException;
import com.clinic.domain.exception.PatientNotFoundException;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.Patient;
import com.clinic.domain.model.TimeSlot;

import java.time.Clock;

/** Orchestrates the use case; business rules stay in the domain, technical details in the adapters. */
public class ScheduleAppointmentService implements ScheduleAppointmentUseCase {

    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final DoctorAvailabilityPort availability;
    private final CoverageVerificationPort coverage;
    private final DomainEventPublisher events;
    private final Clock clock;

    public ScheduleAppointmentService(PatientRepository patients, AppointmentRepository appointments,
                                      DoctorAvailabilityPort availability, CoverageVerificationPort coverage,
                                      DomainEventPublisher events, Clock clock) {
        this.patients = patients;
        this.appointments = appointments;
        this.availability = availability;
        this.coverage = coverage;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public Appointment schedule(Command command) {
        Patient patient = patients.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(command.patientId()));

        if (!coverage.hasActiveCoverage(patient.documentId())) {
            throw new PatientNotCoveredException(patient.id());
        }

        TimeSlot slot = new TimeSlot(command.startsAt(), command.endsAt());
        if (!availability.isAvailable(command.doctorId(), slot)) {
            throw new DoctorNotAvailableException(command.doctorId(), slot);
        }

        Appointment appointment = Appointment.schedule(patient.id(), command.doctorId(), slot,
                command.reason(), clock.instant());
        Appointment saved = appointments.save(appointment);

        events.publish(new AppointmentScheduled(saved.id(), saved.patientId(), saved.doctorId(),
                saved.slot(), clock.instant()));
        return saved;
    }
}
