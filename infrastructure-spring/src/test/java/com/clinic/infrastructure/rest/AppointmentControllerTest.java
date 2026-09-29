package com.clinic.infrastructure.rest;

import com.clinic.application.port.in.CancelAppointmentUseCase;
import com.clinic.application.port.in.ListPatientAppointmentsUseCase;
import com.clinic.application.port.in.ScheduleAppointmentUseCase;
import com.clinic.domain.exception.DoctorNotAvailableException;
import com.clinic.domain.exception.PatientNotCoveredException;
import com.clinic.domain.model.DoctorId;
import com.clinic.domain.model.PatientId;
import com.clinic.domain.model.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ScheduleAppointmentUseCase schedule;
    @MockBean CancelAppointmentUseCase cancel;
    @MockBean ListPatientAppointmentsUseCase list;

    private String body() {
        return """
                {"patientId":"%s","doctorId":"%s","startsAt":"2030-01-02T09:00:00Z",
                 "endsAt":"2030-01-02T09:30:00Z","reason":"Revisión"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    void returns409WhenDoctorIsNotAvailable() throws Exception {
        TimeSlot slot = new TimeSlot(Instant.parse("2030-01-02T09:00:00Z"), Instant.parse("2030-01-02T09:30:00Z"));
        when(schedule.schedule(any())).thenThrow(new DoctorNotAvailableException(DoctorId.newId(), slot));

        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isConflict());
    }

    @Test
    void returns422WhenPatientHasNoCoverage() throws Exception {
        when(schedule.schedule(any())).thenThrow(new PatientNotCoveredException(PatientId.newId()));

        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void returns400WhenBodyIsInvalid() throws Exception {
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
