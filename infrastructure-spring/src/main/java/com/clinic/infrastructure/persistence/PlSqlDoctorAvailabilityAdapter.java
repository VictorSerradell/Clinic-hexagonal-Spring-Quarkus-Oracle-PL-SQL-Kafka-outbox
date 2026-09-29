package com.clinic.infrastructure.persistence;

import com.clinic.application.port.out.DoctorAvailabilityPort;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Delegates the availability check to the Oracle PL/SQL package PKG_APPOINTMENTS (see /db/init). */
@Component
public class PlSqlDoctorAvailabilityAdapter implements DoctorAvailabilityPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(DoctorId doctorId, TimeSlot slot) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("PKG_APPOINTMENTS.PR_IS_DOCTOR_AVAILABLE")
                .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(2, OffsetDateTime.class, ParameterMode.IN)
                .registerStoredProcedureParameter(3, OffsetDateTime.class, ParameterMode.IN)
                .registerStoredProcedureParameter(4, Integer.class, ParameterMode.OUT)
                .setParameter(1, doctorId.toString())
                .setParameter(2, slot.start().atOffset(ZoneOffset.UTC))
                .setParameter(3, slot.end().atOffset(ZoneOffset.UTC));

        query.execute();

        Number available = (Number) query.getOutputParameterValue(4);
        return available != null && available.intValue() == 1;
    }
}
