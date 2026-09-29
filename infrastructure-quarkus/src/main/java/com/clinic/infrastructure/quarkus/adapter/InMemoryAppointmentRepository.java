package com.clinic.infrastructure.quarkus.adapter;

import com.clinic.application.port.out.AppointmentRepository;
import com.clinic.application.port.out.DoctorAvailabilityPort;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.AppointmentStatus;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.PatientId;
import com.clinic.domain.model.TimeSlot;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory adapter implementing two ports: persistence and (derived) doctor availability. */
@ApplicationScoped
public class InMemoryAppointmentRepository implements AppointmentRepository, DoctorAvailabilityPort {

    private final Map<AppointmentId, Appointment> store = new ConcurrentHashMap<>();

    @Override
    public Appointment save(Appointment appointment) {
        store.put(appointment.id(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(AppointmentId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Appointment> findByPatientId(PatientId patientId) {
        return store.values().stream()
                .filter(a -> a.patientId().equals(patientId))
                .sorted(Comparator.comparing(a -> a.slot().start()))
                .toList();
    }

    @Override
    public boolean isAvailable(DoctorId doctorId, TimeSlot slot) {
        return store.values().stream().noneMatch(a -> a.doctorId().equals(doctorId)
                && a.status() == AppointmentStatus.SCHEDULED
                && a.slot().overlaps(slot));
    }
}
