package com.clinic.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits AppointmentScheduled, AppointmentCancelled {
    Instant occurredAt();
}
