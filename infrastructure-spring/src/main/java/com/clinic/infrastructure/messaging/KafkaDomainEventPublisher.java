package com.clinic.infrastructure.messaging;

import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.domain.event.AppointmentCancelled;
import com.clinic.domain.event.AppointmentScheduled;
import com.clinic.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper,
                                     @Value("${clinic.events.topic}") String topic) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void publish(DomainEvent event) {
        EventMessage message = toMessage(event);
        try {
            String json = mapper.writeValueAsString(message);
            kafka.send(topic, message.aggregateId(), json).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Could not publish {} to topic {}", message.eventType(), topic, error);
                }
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize event " + message.eventType(), e);
        }
    }

    private static EventMessage toMessage(DomainEvent event) {
        return switch (event) {
            case AppointmentScheduled e -> new EventMessage("AppointmentScheduled", e.appointmentId().toString(),
                    e.occurredAt().toString(), Map.of(
                    "patientId", e.patientId().toString(),
                    "doctorId", e.doctorId().toString(),
                    "startsAt", e.slot().start().toString(),
                    "endsAt", e.slot().end().toString()));
            case AppointmentCancelled e -> new EventMessage("AppointmentCancelled", e.appointmentId().toString(),
                    e.occurredAt().toString(), Map.of("patientId", e.patientId().toString()));
        };
    }
}
