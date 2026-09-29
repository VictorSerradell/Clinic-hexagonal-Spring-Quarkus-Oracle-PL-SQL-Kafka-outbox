package com.clinic.infrastructure.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock OutboxJpaRepository outbox;
    @Mock KafkaTemplate<String, String> kafka;

    private final Instant now = Instant.parse("2030-01-01T10:00:00Z");
    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new OutboxRelay(outbox, kafka, "topic", Clock.fixed(now, ZoneOffset.UTC));
    }

    private OutboxEventEntity event(String id, String aggregateId) {
        return new OutboxEventEntity(id, aggregateId, "AppointmentScheduled", "{\"eventId\":\"" + id + "\"}", now);
    }

    @Test
    void publishesPendingEventsAndMarksThemAsPublished() {
        when(outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(event("e1", "a1"), event("e2", "a2")));
        when(kafka.send(eq("topic"), eq("a1"), eq("{\"eventId\":\"e1\"}")))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(kafka.send(eq("topic"), eq("a2"), eq("{\"eventId\":\"e2\"}")))
                .thenReturn(CompletableFuture.completedFuture(null));

        relay.publishPending();

        verify(outbox).markPublished("e1", now);
        verify(outbox).markPublished("e2", now);
    }

    @Test
    void leavesEventPendingAndStopsWhenKafkaFails() {
        when(outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(event("e1", "a1"), event("e2", "a2")));
        when(kafka.send(eq("topic"), eq("a1"), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        relay.publishPending();

        verify(outbox, never()).markPublished(any(), any());
    }
}
