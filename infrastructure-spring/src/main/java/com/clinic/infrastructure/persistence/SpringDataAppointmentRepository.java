package com.clinic.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAppointmentRepository extends JpaRepository<AppointmentJpaEntity, String> {

    List<AppointmentJpaEntity> findByPatientIdOrderByStartsAtAsc(String patientId);
}
