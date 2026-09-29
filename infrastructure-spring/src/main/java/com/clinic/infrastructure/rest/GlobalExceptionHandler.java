package com.clinic.infrastructure.rest;

import com.clinic.domain.exception.AppointmentNotFoundException;
import com.clinic.domain.exception.AppointmentStateException;
import com.clinic.domain.exception.DomainException;
import com.clinic.domain.exception.DoctorNotAvailableException;
import com.clinic.domain.exception.DuplicatePatientException;
import com.clinic.domain.exception.InvalidDataException;
import com.clinic.domain.exception.PatientNotCoveredException;
import com.clinic.domain.exception.PatientNotFoundException;
import com.clinic.infrastructure.coverage.CoverageUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain exceptions to HTTP problem responses (RFC 7807). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PatientNotFoundException.class, AppointmentNotFoundException.class})
    ProblemDetail notFound(DomainException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DoctorNotAvailableException.class, DuplicatePatientException.class,
            AppointmentStateException.class})
    ProblemDetail conflict(DomainException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PatientNotCoveredException.class)
    ProblemDetail notCovered(PatientNotCoveredException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(InvalidDataException.class)
    ProblemDetail invalid(InvalidDataException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(CoverageUnavailableException.class)
    ProblemDetail coverageUnavailable(CoverageUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The coverage verification service is temporarily unavailable");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request body");
    }
}
