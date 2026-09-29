package com.clinic.domain.model;

import com.clinic.domain.exception.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeSlotTest {

    private static final Instant T0 = Instant.parse("2030-01-01T09:00:00Z");

    @Test
    void detectsOverlap() {
        TimeSlot a = new TimeSlot(T0, T0.plusSeconds(1800));
        TimeSlot b = new TimeSlot(T0.plusSeconds(900), T0.plusSeconds(2700));
        assertThat(a.overlaps(b)).isTrue();
    }

    @Test
    void adjacentSlotsDoNotOverlap() {
        TimeSlot a = new TimeSlot(T0, T0.plusSeconds(1800));
        TimeSlot b = new TimeSlot(T0.plusSeconds(1800), T0.plusSeconds(3600));
        assertThat(a.overlaps(b)).isFalse();
    }

    @Test
    void rejectsInvertedOrTooLongSlots() {
        assertThatThrownBy(() -> new TimeSlot(T0, T0)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> new TimeSlot(T0, T0.plusSeconds(3 * 3600))).isInstanceOf(InvalidDataException.class);
    }
}
