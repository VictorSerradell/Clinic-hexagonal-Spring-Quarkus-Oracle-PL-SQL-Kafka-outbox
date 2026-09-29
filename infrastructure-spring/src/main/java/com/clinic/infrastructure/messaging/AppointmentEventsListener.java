package com.clinic.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Inbound Kafka adapter. Here it only logs a "notification"; a real one would call a use case. */
@Component
public class AppointmentEventsListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventsListener.class);

    @KafkaListener(topics = "${clinic.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String payload) {
        log.info("Notification to send to patient/doctor: {}", payload);
    }
}
