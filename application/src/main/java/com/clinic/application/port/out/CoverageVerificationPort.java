package com.clinic.application.port.out;

/** Outbound port: checks the patient's insurance/health coverage in an external system (REST). */
public interface CoverageVerificationPort {

    boolean hasActiveCoverage(String documentId);
}
