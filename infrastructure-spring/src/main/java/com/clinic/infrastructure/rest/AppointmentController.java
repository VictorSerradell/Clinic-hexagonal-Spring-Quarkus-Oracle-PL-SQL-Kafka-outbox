package com.clinic.infrastructure.rest;

import com.clinic.application.port.in.CancelAppointmentUseCase;
import com.clinic.application.port.in.ListPatientAppointmentsUseCase;
import com.clinic.application.port.in.ScheduleAppointmentUseCase;
import com.clinic.domain.model.AppointmentId;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.PatientId;
import com.clinic.infrastructure.rest.dto.AppointmentResponse;
import com.clinic.infrastructure.rest.dto.ScheduleAppointmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final ScheduleAppointmentUseCase scheduleAppointment;
    private final CancelAppointmentUseCase cancelAppointment;
    private final ListPatientAppointmentsUseCase listAppointments;

    public AppointmentController(ScheduleAppointmentUseCase scheduleAppointment,
                                 CancelAppointmentUseCase cancelAppointment,
                                 ListPatientAppointmentsUseCase listAppointments) {
        this.scheduleAppointment = scheduleAppointment;
        this.cancelAppointment = cancelAppointment;
        this.listAppointments = listAppointments;
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse schedule(@Valid @RequestBody ScheduleAppointmentRequest request) {
        return AppointmentResponse.from(scheduleAppointment.schedule(new ScheduleAppointmentUseCase.Command(
                new PatientId(request.patientId()), new DoctorId(request.doctorId()),
                request.startsAt(), request.endsAt(), request.reason())));
    }

    @PostMapping("/appointments/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id) {
        return AppointmentResponse.from(cancelAppointment.cancel(new AppointmentId(id)));
    }

    @GetMapping("/patients/{patientId}/appointments")
    public List<AppointmentResponse> list(@PathVariable UUID patientId) {
        return listAppointments.list(new PatientId(patientId)).stream().map(AppointmentResponse::from).toList();
    }
}
