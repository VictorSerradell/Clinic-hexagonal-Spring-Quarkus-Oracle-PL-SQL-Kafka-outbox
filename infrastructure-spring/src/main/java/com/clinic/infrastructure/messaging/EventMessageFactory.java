package com.clinic.infrastructure.messaging;

import com.clinic.domain.event.AppointmentCancelled;
import com.clinic.domain.event.AppointmentScheduled;
import com.clinic.domain.event.DomainEvent;

import java.util.Map;

/** Maps domain events to their wire format. */
public final class EventMessageFactory {

    private EventMessageFactory() {
    }

    public static EventMessage from(DomainEvent event, String eventId) {
        return switch (event) {
            case AppointmentScheduled e -> new EventMessage(eventId, "AppointmentScheduled",
                    e.appointmentId().toString(), e.occurredAt().toString(), Map.of(
                    "patientId", e.patientId().toString(),
                    "doctorId", e.doctorId().toString(),
                    "startsAt", e.slot().start().toString(),
                    "endsAt", e.slot().end().toString()));
            case AppointmentCancelled e -> new EventMessage(eventId, "AppointmentCancelled",
                    e.appointmentId().toString(), e.occurredAt().toString(),
                    Map.of("patientId", e.patientId().toString()));
        };
    }
}
