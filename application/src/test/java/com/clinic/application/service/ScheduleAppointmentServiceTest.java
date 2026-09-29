package com.clinic.application.service;

import com.clinic.application.port.in.ScheduleAppointmentUseCase.Command;
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
import com.clinic.domain.model.AppointmentStatus;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleAppointmentServiceTest {

    @Mock PatientRepository patients;
    @Mock AppointmentRepository appointments;
    @Mock DoctorAvailabilityPort availability;
    @Mock CoverageVerificationPort coverage;
    @Mock DomainEventPublisher events;

    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
    private final Patient patient = Patient.register("Ana Pérez", "12345678Z", LocalDate.of(1990, 1, 1));
    private final DoctorId doctor = DoctorId.newId();
    private final Command command = new Command(patient.id(), doctor,
            Instant.parse("2030-01-02T09:00:00Z"), Instant.parse("2030-01-02T09:30:00Z"), "Revisión");

    private ScheduleAppointmentService service;

    @BeforeEach
    void setUp() {
        service = new ScheduleAppointmentService(patients, appointments, availability, coverage, events, clock);
    }

    @Test
    void schedulesAndPublishesEvent() {
        when(patients.findById(patient.id())).thenReturn(Optional.of(patient));
        when(coverage.hasActiveCoverage(patient.documentId())).thenReturn(true);
        when(availability.isAvailable(eq(doctor), any())).thenReturn(true);
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.schedule(command);

        assertThat(result.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(events).publish(any(AppointmentScheduled.class));
    }

    @Test
    void failsWhenPatientDoesNotExist() {
        when(patients.findById(patient.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.schedule(command)).isInstanceOf(PatientNotFoundException.class);
        verifyNoInteractions(coverage, availability, events);
    }

    @Test
    void failsWhenPatientHasNoCoverage() {
        when(patients.findById(patient.id())).thenReturn(Optional.of(patient));
        when(coverage.hasActiveCoverage(patient.documentId())).thenReturn(false);

        assertThatThrownBy(() -> service.schedule(command)).isInstanceOf(PatientNotCoveredException.class);
        verifyNoInteractions(availability, events);
    }

    @Test
    void failsWhenDoctorIsNotAvailable() {
        when(patients.findById(patient.id())).thenReturn(Optional.of(patient));
        when(coverage.hasActiveCoverage(patient.documentId())).thenReturn(true);
        when(availability.isAvailable(eq(doctor), any())).thenReturn(false);

        assertThatThrownBy(() -> service.schedule(command)).isInstanceOf(DoctorNotAvailableException.class);
        verify(appointments, never()).save(any());
        verifyNoInteractions(events);
    }
}
