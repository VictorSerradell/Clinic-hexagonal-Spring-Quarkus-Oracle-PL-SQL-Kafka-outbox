package com.clinic.application.port.out;

import com.clinic.domain.event.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
