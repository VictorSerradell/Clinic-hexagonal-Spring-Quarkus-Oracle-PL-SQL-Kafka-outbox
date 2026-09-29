package com.clinic.infrastructure.quarkus.rest;

import com.clinic.domain.exception.AppointmentNotFoundException;
import com.clinic.domain.exception.AppointmentStateException;
import com.clinic.domain.exception.DomainException;
import com.clinic.domain.exception.DoctorNotAvailableException;
import com.clinic.domain.exception.DuplicatePatientException;
import com.clinic.domain.exception.PatientNotCoveredException;
import com.clinic.domain.exception.PatientNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Map;

public class DomainExceptionMapper {

    @ServerExceptionMapper
    public Response map(DomainException e) {
        int status = switch (e) {
            case PatientNotFoundException x -> 404;
            case AppointmentNotFoundException x -> 404;
            case DoctorNotAvailableException x -> 409;
            case DuplicatePatientException x -> 409;
            case AppointmentStateException x -> 409;
            case PatientNotCoveredException x -> 422;
            default -> 400;
        };
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", e.getMessage())).build();
    }
}
