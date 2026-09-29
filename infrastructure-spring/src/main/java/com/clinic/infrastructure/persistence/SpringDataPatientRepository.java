package com.clinic.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPatientRepository extends JpaRepository<PatientJpaEntity, String> {

    Optional<PatientJpaEntity> findByDocumentId(String documentId);
}
