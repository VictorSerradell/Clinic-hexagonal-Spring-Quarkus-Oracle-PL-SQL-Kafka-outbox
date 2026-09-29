package com.clinic.infrastructure.quarkus;

import com.clinic.application.port.in.CancelAppointmentUseCase;
import com.clinic.application.port.in.ListPatientAppointmentsUseCase;
import com.clinic.application.port.in.RegisterPatientUseCase;
import com.clinic.application.port.in.ScheduleAppointmentUseCase;
import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.application.port.out.CoverageVerificationPort;
import com.clinic.application.port.out.DoctorAvailabilityPort;
import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.application.port.out.PatientRepository;
import com.clinic.application.service.CancelAppointmentService;
import com.clinic.application.service.ListPatientAppointmentsService;
import com.clinic.application.service.RegisterPatientService;
import com.clinic.application.service.ScheduleAppointmentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/** Composition root for Quarkus: exposes the framework-free use cases as CDI beans. */
@ApplicationScoped
public class UseCaseProducers {

    @Produces
    @Singleton
    Clock clock() {
        return Clock.systemUTC();
    }

    @Produces
    @Singleton
    RegisterPatientUseCase registerPatient(PatientRepository patients) {
        return new RegisterPatientService(patients);
    }

    @Produces
    @Singleton
    ScheduleAppointmentUseCase scheduleAppointment(PatientRepository patients, AppointmentRepository appointments,
                                                   DoctorAvailabilityPort availability,
                                                   CoverageVerificationPort coverage,
                                                   DomainEventPublisher events, Clock clock) {
        return new ScheduleAppointmentService(patients, appointments, availability, coverage, events, clock);
    }

    @Produces
    @Singleton
    CancelAppointmentUseCase cancelAppointment(AppointmentRepository appointments, DomainEventPublisher events,
                                               Clock clock) {
        return new CancelAppointmentService(appointments, events, clock);
    }

    @Produces
    @Singleton
    ListPatientAppointmentsUseCase listAppointments(PatientRepository patients, AppointmentRepository appointments) {
        return new ListPatientAppointmentsService(patients, appointments);
    }
}
