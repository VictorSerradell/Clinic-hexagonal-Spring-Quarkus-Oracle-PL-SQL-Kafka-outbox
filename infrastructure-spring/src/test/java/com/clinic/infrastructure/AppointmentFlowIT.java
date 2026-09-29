package com.clinic.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end test against real Oracle (schema + PL/SQL package from /db/init) and Kafka.
 * Skipped automatically when Docker is not available.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class AppointmentFlowIT {

    @Container
    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withUsername("clinic")
            .withPassword("clinic")
            .withCopyFileToContainer(initScript("01_schema.sql"), "/container-entrypoint-initdb.d/01_schema.sql")
            .withCopyFileToContainer(initScript("02_pkg_appointments.sql"),
                    "/container-entrypoint-initdb.d/02_pkg_appointments.sql")
            .withCopyFileToContainer(initScript("03_outbox.sql"), "/container-entrypoint-initdb.d/03_outbox.sql");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", ORACLE::getJdbcUrl);
        registry.add("spring.datasource.username", ORACLE::getUsername);
        registry.add("spring.datasource.password", ORACLE::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired TestRestTemplate rest;
    @Autowired JdbcTemplate jdbc;
    @Value("${clinic.security.jwt-secret}") String jwtSecret;

    @Test
    void schedulingUsesPlSqlAvailabilityAndCancellationFreesTheSlot() throws Exception {
        ResponseEntity<Map> patient = post("/api/patients", Map.of(
                "fullName", "Ana Pérez", "documentId", "12345678Z", "birthDate", "1990-01-01"));
        assertThat(patient.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String patientId = (String) patient.getBody().get("id");

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        Map<String, Object> request = Map.of("patientId", patientId, "doctorId", UUID.randomUUID().toString(),
                "startsAt", start.toString(), "endsAt", start.plus(30, ChronoUnit.MINUTES).toString(),
                "reason", "Revisión");

        ResponseEntity<Map> first = post("/api/appointments", request);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(post("/api/appointments", request).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<List> list = rest.exchange("/api/patients/" + patientId + "/appointments",
                HttpMethod.GET, new HttpEntity<>(headers()), List.class);
        assertThat(list.getBody()).hasSize(1);

        String appointmentId = (String) first.getBody().get("id");
        assertThat(post("/api/appointments/" + appointmentId + "/cancel", null).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(post("/api/appointments", request).getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Outbox: scheduled + cancelled + scheduled again = 3 events, all forwarded to Kafka by the relay
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(jdbc.queryForObject("select count(*) from outbox_events", Integer.class)).isEqualTo(3);
            assertThat(jdbc.queryForObject(
                    "select count(*) from outbox_events where published_at is null", Integer.class)).isZero();
        });
    }

    @Test
    void rejectsRequestsWithoutToken() {
        ResponseEntity<String> response = rest.getForEntity(
                "/api/patients/" + UUID.randomUUID() + "/appointments", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Map> post(String path, Object body) throws JOSEException {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers()), Map.class);
    }

    private HttpHeaders headers() throws JOSEException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token());
        return headers;
    }

    private String token() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("integration-test")
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    private static MountableFile initScript(String name) {
        return MountableFile.forHostPath(Path.of("..", "db", "init", name).toAbsolutePath().normalize());
    }
}
