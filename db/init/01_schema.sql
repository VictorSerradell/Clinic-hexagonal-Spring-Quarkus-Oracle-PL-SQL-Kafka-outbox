-- Executed by the gvenzl/oracle-free container on first start
-- init scripts run as SYS in the CDB root: switch to the pluggable DB first
ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = clinic;

CREATE TABLE patients (
    id           VARCHAR2(36)  PRIMARY KEY,
    full_name    VARCHAR2(200) NOT NULL,
    document_id  VARCHAR2(30)  NOT NULL,
    birth_date   DATE          NOT NULL,
    CONSTRAINT uq_patients_document UNIQUE (document_id)
);

CREATE TABLE appointments (
    id          VARCHAR2(36)  PRIMARY KEY,
    patient_id  VARCHAR2(36)  NOT NULL REFERENCES patients (id),
    doctor_id   VARCHAR2(36)  NOT NULL,
    starts_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    reason      VARCHAR2(500) NOT NULL,
    status      VARCHAR2(20)  NOT NULL CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX ix_appointments_doctor_slot ON appointments (doctor_id, starts_at, ends_at);
CREATE INDEX ix_appointments_patient     ON appointments (patient_id);
