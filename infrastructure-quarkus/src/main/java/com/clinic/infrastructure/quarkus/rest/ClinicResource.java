package com.clinic.infrastructure.quarkus.rest;

import com.clinic.application.port.in.CancelAppointmentUseCase;
import com.clinic.application.port.in.ListPatientAppointmentsUseCase;
import com.clinic.application.port.in.RegisterPatientUseCase;
import com.clinic.application.port.in.ScheduleAppointmentUseCase;
import com.clinic.domain.model.Appointment;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.Patient;
import com.clinic.domain.model.PatientId;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClinicResource {

    @Inject RegisterPatientUseCase registerPatient;
    @Inject ScheduleAppointmentUseCase scheduleAppointment;
    @Inject CancelAppointmentUseCase cancelAppointment;
    @Inject ListPatientAppointmentsUseCase listAppointments;

    @POST
    @Path("/patients")
    public Response register(RegisterPatientRequest request) {
        Patient patient = registerPatient.register(new RegisterPatientUseCase.Command(
                request.fullName(), request.documentId(), request.birthDate()));
        return Response.status(Response.Status.CREATED).entity(PatientResponse.from(patient)).build();
    }

    @POST
    @Path("/appointments")
    public Response schedule(ScheduleAppointmentRequest request) {
        Appointment appointment = scheduleAppointment.schedule(new ScheduleAppointmentUseCase.Command(
                new PatientId(request.patientId()), new DoctorId(request.doctorId()),
                request.startsAt(), request.endsAt(), request.reason()));
        return Response.status(Response.Status.CREATED).entity(AppointmentResponse.from(appointment)).build();
    }

    @POST
    @Path("/appointments/{id}/cancel")
    public AppointmentResponse cancel(@PathParam("id") UUID id) {
        return AppointmentResponse.from(cancelAppointment.cancel(new AppointmentId(id)));
    }

    @GET
    @Path("/patients/{id}/appointments")
    public List<AppointmentResponse> list(@PathParam("id") UUID patientId) {
        return listAppointments.list(new PatientId(patientId)).stream().map(AppointmentResponse::from).toList();
    }

    public record RegisterPatientRequest(String fullName, String documentId, LocalDate birthDate) {
    }

    public record ScheduleAppointmentRequest(UUID patientId, UUID doctorId, Instant startsAt, Instant endsAt,
                                             String reason) {
    }

    public record PatientResponse(UUID id, String fullName, String documentId, LocalDate birthDate) {
        static PatientResponse from(Patient p) {
            return new PatientResponse(p.id().value(), p.fullName(), p.documentId(), p.birthDate());
        }
    }

    public record AppointmentResponse(UUID id, UUID patientId, UUID doctorId, Instant startsAt, Instant endsAt,
                                      String reason, String status) {
        static AppointmentResponse from(Appointment a) {
            return new AppointmentResponse(a.id().value(), a.patientId().value(), a.doctorId().value(),
                    a.slot().start(), a.slot().end(), a.reason(), a.status().name());
        }
    }
}
