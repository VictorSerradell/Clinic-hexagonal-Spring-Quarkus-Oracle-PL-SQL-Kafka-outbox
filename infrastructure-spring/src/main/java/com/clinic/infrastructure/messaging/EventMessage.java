package com.clinic.infrastructure.messaging;

import java.util.Map;

/**
 * Wire format of the events published to Kafka (decoupled from the domain model).
 * {@code eventId} is unique per event: consumers can use it to deduplicate (delivery is at-least-once).
 */
public record EventMessage(String eventId, String eventType, String aggregateId, String occurredAt,
                           Map<String, String> payload) {
}
