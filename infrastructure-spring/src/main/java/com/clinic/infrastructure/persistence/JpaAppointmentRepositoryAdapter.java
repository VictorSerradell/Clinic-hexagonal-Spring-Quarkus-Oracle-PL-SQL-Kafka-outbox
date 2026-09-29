package com.clinic.infrastructure.persistence;

import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.PatientId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class JpaAppointmentRepositoryAdapter implements AppointmentRepository {

    private final SpringDataAppointmentRepository jpa;

    public JpaAppointmentRepositoryAdapter(SpringDataAppointmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Appointment save(Appointment appointment) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(appointment)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(AppointmentId id) {
        return jpa.findById(id.toString()).map(PersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPatientId(PatientId patientId) {
        return jpa.findByPatientIdOrderByStartsAtAsc(patientId.toString()).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
