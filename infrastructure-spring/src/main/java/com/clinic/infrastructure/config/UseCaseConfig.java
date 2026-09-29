package com.clinic.infrastructure.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * Composition root: the application module knows nothing about Spring, so the use cases
 * are wired here as plain objects. Use cases that publish events run inside ONE transaction, so the
 * business change and its outbox row are committed (or rolled back) together.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RegisterPatientUseCase registerPatientUseCase(PatientRepository patients) {
        return new RegisterPatientService(patients);
    }

    @Bean
    ScheduleAppointmentUseCase scheduleAppointmentUseCase(PatientRepository patients,
                                                          AppointmentRepository appointments,
                                                          DoctorAvailabilityPort availability,
                                                          CoverageVerificationPort coverage,
                                                          DomainEventPublisher events,
                                                          Clock clock,
                                                          TransactionTemplate tx) {
        ScheduleAppointmentUseCase target =
                new ScheduleAppointmentService(patients, appointments, availability, coverage, events, clock);
        return command -> tx.execute(status -> target.schedule(command));
    }

    @Bean
    CancelAppointmentUseCase cancelAppointmentUseCase(AppointmentRepository appointments,
                                                      DomainEventPublisher events, Clock clock,
                                                      TransactionTemplate tx) {
        CancelAppointmentUseCase target = new CancelAppointmentService(appointments, events, clock);
        return appointmentId -> tx.execute(status -> target.cancel(appointmentId));
    }

    @Bean
    ListPatientAppointmentsUseCase listPatientAppointmentsUseCase(PatientRepository patients,
                                                                  AppointmentRepository appointments) {
        return new ListPatientAppointmentsService(patients, appointments);
    }
}
