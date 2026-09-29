package com.clinic.domain.model;

import com.clinic.domain.exception.InvalidDataException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record TimeSlot(Instant start, Instant end) {

    public static final Duration MAX_DURATION = Duration.ofHours(2);

    public TimeSlot {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) {
            throw new InvalidDataException("The end of the slot must be after its start");
        }
        if (Duration.between(start, end).compareTo(MAX_DURATION) > 0) {
            throw new InvalidDataException("An appointment cannot last more than " + MAX_DURATION.toHours() + " hours");
        }
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
