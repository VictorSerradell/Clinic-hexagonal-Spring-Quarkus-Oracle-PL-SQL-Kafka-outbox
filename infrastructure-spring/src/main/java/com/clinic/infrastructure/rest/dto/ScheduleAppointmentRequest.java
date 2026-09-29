package com.clinic.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ScheduleAppointmentRequest(@NotNull UUID patientId, @NotNull UUID doctorId,
                                         @NotNull Instant startsAt, @NotNull Instant endsAt,
                                         @NotBlank String reason) {
}
