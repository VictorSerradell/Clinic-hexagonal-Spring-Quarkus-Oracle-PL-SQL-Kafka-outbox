package com.clinic.infrastructure.coverage;

/** The external coverage API could not be reached (timeout, connection error or 5xx). */
public class CoverageUnavailableException extends RuntimeException {

    public CoverageUnavailableException(Throwable cause) {
        super("Coverage verification service unavailable", cause);
    }
}
