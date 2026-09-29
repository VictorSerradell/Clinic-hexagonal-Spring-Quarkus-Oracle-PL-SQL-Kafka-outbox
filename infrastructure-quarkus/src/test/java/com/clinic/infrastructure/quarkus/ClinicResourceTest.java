package com.clinic.infrastructure.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class ClinicResourceTest {

    @Test
    void registersPatientSchedulesAndRejectsOverlap() {
        String patientId = given().contentType("application/json")
                .body(Map.of("fullName", "Ana Pérez", "documentId", "12345678Z", "birthDate", "1990-01-01"))
                .when().post("/api/patients")
                .then().statusCode(201)
                .extract().path("id");

        String doctorId = UUID.randomUUID().toString();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        Map<String, Object> body = Map.of("patientId", patientId, "doctorId", doctorId,
                "startsAt", start.toString(), "endsAt", start.plus(30, ChronoUnit.MINUTES).toString(),
                "reason", "Revisión");

        given().contentType("application/json").body(body)
                .when().post("/api/appointments")
                .then().statusCode(201).body("status", equalTo("SCHEDULED"));

        given().contentType("application/json").body(body)
                .when().post("/api/appointments")
                .then().statusCode(409);
    }
}
