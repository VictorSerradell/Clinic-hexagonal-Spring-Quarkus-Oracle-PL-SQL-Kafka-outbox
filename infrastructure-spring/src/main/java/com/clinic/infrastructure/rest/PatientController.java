package com.clinic.infrastructure.rest;

import com.clinic.application.port.in.RegisterPatientUseCase;
import com.clinic.infrastructure.rest.dto.PatientResponse;
import com.clinic.infrastructure.rest.dto.RegisterPatientRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final RegisterPatientUseCase registerPatient;

    public PatientController(RegisterPatientUseCase registerPatient) {
        this.registerPatient = registerPatient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse register(@Valid @RequestBody RegisterPatientRequest request) {
        return PatientResponse.from(registerPatient.register(
                new RegisterPatientUseCase.Command(request.fullName(), request.documentId(), request.birthDate())));
    }
}
