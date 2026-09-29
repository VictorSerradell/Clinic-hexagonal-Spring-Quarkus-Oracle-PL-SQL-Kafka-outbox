package com.clinic.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
public class PatientJpaEntity {

    @Id
    private String id;
    private String fullName;
    private String documentId;
    private LocalDate birthDate;

    protected PatientJpaEntity() {
    }

    public PatientJpaEntity(String id, String fullName, String documentId, LocalDate birthDate) {
        this.id = id;
        this.fullName = fullName;
        this.documentId = documentId;
        this.birthDate = birthDate;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getDocumentId() { return documentId; }
    public LocalDate getBirthDate() { return birthDate; }
}
