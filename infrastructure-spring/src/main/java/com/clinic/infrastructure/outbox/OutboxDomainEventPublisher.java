package com.clinic.infrastructure.outbox;

import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.domain.event.DomainEvent;
import com.clinic.infrastructure.messaging.EventMessage;
import com.clinic.infrastructure.messaging.EventMessageFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Transactional Outbox: instead of talking to Kafka, the event is stored in the same database
 * transaction as the business change. {@link OutboxRelay} forwards it to Kafka afterwards.
 */
@Component
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxJpaRepository outbox;
    private final ObjectMapper mapper;

    public OutboxDomainEventPublisher(OutboxJpaRepository outbox, ObjectMapper mapper) {
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)   // must join the use case transaction
    public void publish(DomainEvent event) {
        String eventId = UUID.randomUUID().toString();
        EventMessage message = EventMessageFactory.from(event, eventId);
        try {
            outbox.save(new OutboxEventEntity(eventId, message.aggregateId(), message.eventType(),
                    mapper.writeValueAsString(message), event.occurredAt()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize event " + message.eventType(), e);
        }
    }
}
