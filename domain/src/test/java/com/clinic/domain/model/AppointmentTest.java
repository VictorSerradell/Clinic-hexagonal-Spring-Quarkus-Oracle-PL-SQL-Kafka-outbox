package com.clinic.domain.model;

import com.clinic.domain.exception.AppointmentStateException;
import com.clinic.domain.exception.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentTest {

    private static final Instant NOW = Instant.parse("2030-01-01T10:00:00Z");
    private final TimeSlot futureSlot = new TimeSlot(NOW.plus(1, ChronoUnit.DAYS), NOW.plus(1, ChronoUnit.DAYS).plusSeconds(1800));

    private Appointment scheduled() {
        return Appointment.schedule(PatientId.newId(), DoctorId.newId(), futureSlot, "Revisión anual", NOW);
    }

    @Test
    void schedulesAnAppointmentInTheFuture() {
        assertThat(scheduled().status()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void rejectsAppointmentsInThePast() {
        TimeSlot past = new TimeSlot(NOW.minusSeconds(3600), NOW.minusSeconds(1800));
        assertThatThrownBy(() -> Appointment.schedule(PatientId.newId(), DoctorId.newId(), past, "Dolor", NOW))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void cancelsAScheduledAppointment() {
        Appointment appointment = scheduled();
        appointment.cancel(NOW);
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cannotCancelTwice() {
        Appointment appointment = scheduled();
        appointment.cancel(NOW);
        assertThatThrownBy(() -> appointment.cancel(NOW)).isInstanceOf(AppointmentStateException.class);
    }

    @Test
    void cannotCancelOnceStarted() {
        Appointment appointment = scheduled();
        assertThatThrownBy(() -> appointment.cancel(futureSlot.start().plusSeconds(1)))
                .isInstanceOf(AppointmentStateException.class);
    }
}
