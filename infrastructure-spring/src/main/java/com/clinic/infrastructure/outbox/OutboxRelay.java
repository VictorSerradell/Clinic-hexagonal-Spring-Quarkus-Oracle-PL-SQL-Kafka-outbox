package com.clinic.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Polls pending outbox rows and publishes them to Kafka, oldest first.
 * Delivery is at-least-once: if the app dies after Kafka acknowledges but before the row is marked,
 * the event is sent again (consumers deduplicate by eventId). With several replicas the same row can
 * be sent twice; use SELECT ... FOR UPDATE SKIP LOCKED or a scheduler lock (ShedLock) to avoid it.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxJpaRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final String topic;
    private final Clock clock;

    public OutboxRelay(OutboxJpaRepository outbox, KafkaTemplate<String, String> kafka,
                       @Value("${clinic.events.topic}") String topic, Clock clock) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.topic = topic;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${clinic.outbox.poll-interval-ms:1000}")
    public void publishPending() {
        List<OutboxEventEntity> pending = outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEventEntity event : pending) {
            try {
                kafka.send(topic, event.getAggregateId(), event.getPayload()).get(5, TimeUnit.SECONDS);
                outbox.markPublished(event.getId(), clock.instant());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                // Stop here to keep ordering; the same event is retried on the next tick.
                log.warn("Could not publish outbox event {} ({}); will retry", event.getId(), event.getEventType(), e);
                return;
            }
        }
    }
}
