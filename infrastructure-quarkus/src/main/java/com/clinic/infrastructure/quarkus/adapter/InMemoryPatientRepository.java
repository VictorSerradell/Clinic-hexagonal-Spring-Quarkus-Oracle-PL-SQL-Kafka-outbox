package com.clinic.infrastructure.quarkus.adapter;

import com.clinic.application.port.out.PatientRepository;
import com.clinic.domain.model.Patient;
import com.clinic.domain.model.PatientId;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryPatientRepository implements PatientRepository {

    private final Map<PatientId, Patient> store = new ConcurrentHashMap<>();

    @Override
    public Patient save(Patient patient) {
        store.put(patient.id(), patient);
        return patient;
    }

    @Override
    public Optional<Patient> findById(PatientId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Patient> findByDocumentId(String documentId) {
        return store.values().stream().filter(p -> p.documentId().equals(documentId)).findFirst();
    }
}
