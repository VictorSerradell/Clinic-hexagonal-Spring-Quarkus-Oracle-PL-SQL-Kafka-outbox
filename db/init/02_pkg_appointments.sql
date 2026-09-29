-- init scripts run as SYS in the CDB root: switch to the pluggable DB first
ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = clinic;

CREATE OR REPLACE PACKAGE pkg_appointments AS
    /**
     * Sets p_available = 1 when the doctor has no SCHEDULED appointment overlapping [p_starts_at, p_ends_at).
     */
    PROCEDURE pr_is_doctor_available (
        p_doctor_id  IN  VARCHAR2,
        p_starts_at  IN  TIMESTAMP WITH TIME ZONE,
        p_ends_at    IN  TIMESTAMP WITH TIME ZONE,
        p_available  OUT NUMBER
    );
END pkg_appointments;
/

CREATE OR REPLACE PACKAGE BODY pkg_appointments AS

    PROCEDURE pr_is_doctor_available (
        p_doctor_id  IN  VARCHAR2,
        p_starts_at  IN  TIMESTAMP WITH TIME ZONE,
        p_ends_at    IN  TIMESTAMP WITH TIME ZONE,
        p_available  OUT NUMBER
    ) IS
        v_conflicts NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_conflicts
          FROM appointments
         WHERE doctor_id = p_doctor_id
           AND status    = 'SCHEDULED'
           AND starts_at < p_ends_at
           AND ends_at   > p_starts_at;

        p_available := CASE WHEN v_conflicts = 0 THEN 1 ELSE 0 END;
    END pr_is_doctor_available;

END pkg_appointments;
/
