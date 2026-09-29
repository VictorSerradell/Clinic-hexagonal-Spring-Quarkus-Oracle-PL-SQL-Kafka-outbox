package com.clinic.infrastructure.persistence;

import com.clinic.application.port.out.PatientRepository;
import com.clinic.domain.model.Patient;
import com.clinic.domain.model.PatientId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class JpaPatientRepositoryAdapter implements PatientRepository {

    private final SpringDataPatientRepository jpa;

    public JpaPatientRepositoryAdapter(SpringDataPatientRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Patient save(Patient patient) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(patient)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findById(PatientId id) {
        return jpa.findById(id.toString()).map(PersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findByDocumentId(String documentId) {
        return jpa.findByDocumentId(documentId).map(PersistenceMapper::toDomain);
    }
}
