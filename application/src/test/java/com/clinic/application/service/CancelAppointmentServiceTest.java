package com.clinic.application.service;

import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.domain.event.AppointmentCancelled;
import com.clinic.domain.exception.AppointmentNotFoundException;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.AppointmentStatus;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.PatientId;
import com.clinic.domain.model.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentServiceTest {

    @Mock AppointmentRepository appointments;
    @Mock DomainEventPublisher events;

    private final Instant now = Instant.parse("2030-01-01T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    private CancelAppointmentService service;

    @BeforeEach
    void setUp() {
        service = new CancelAppointmentService(appointments, events, clock);
    }

    @Test
    void cancelsAndPublishesEvent() {
        Appointment appointment = Appointment.schedule(PatientId.newId(), DoctorId.newId(),
                new TimeSlot(Instant.parse("2030-01-02T09:00:00Z"), Instant.parse("2030-01-02T09:30:00Z")),
                "Revisión", now);
        when(appointments.findById(appointment.id())).thenReturn(Optional.of(appointment));
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.cancel(appointment.id());

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(events).publish(any(AppointmentCancelled.class));
    }

    @Test
    void failsWhenAppointmentDoesNotExist() {
        AppointmentId id = AppointmentId.newId();
        when(appointments.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(id)).isInstanceOf(AppointmentNotFoundException.class);
    }
}
