package com.clinic.infrastructure.outbox;

import com.clinic.domain.event.AppointmentCancelled;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.PatientId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxDomainEventPublisherTest {

    @Mock OutboxJpaRepository outbox;

    @Test
    void storesTheEventAsJsonInTheOutbox() {
        OutboxDomainEventPublisher publisher = new OutboxDomainEventPublisher(outbox, new ObjectMapper());
        AppointmentId appointmentId = AppointmentId.newId();

        publisher.publish(new AppointmentCancelled(appointmentId, PatientId.newId(),
                Instant.parse("2030-01-01T10:00:00Z")));

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outbox).save(captor.capture());
        OutboxEventEntity saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo("AppointmentCancelled");
        assertThat(saved.getAggregateId()).isEqualTo(appointmentId.toString());
        assertThat(saved.getPayload()).contains("\"eventId\"", "AppointmentCancelled");
        assertThat(saved.getPublishedAt()).isNull();
    }
}
