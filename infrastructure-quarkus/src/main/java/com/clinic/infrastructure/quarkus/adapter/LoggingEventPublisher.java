package com.clinic.infrastructure.quarkus.adapter;

import com.clinic.application.port.out.DomainEventPublisher;
import com.clinic.domain.event.DomainEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/** Swap for a quarkus-messaging-kafka (SmallRye Reactive Messaging) adapter to publish to Kafka from Quarkus. */
@ApplicationScoped
public class LoggingEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = Logger.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        LOG.infof("Domain event: %s", event);
    }
}
