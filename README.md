# Clinic Hexagonal

Gestión de citas médicas construida con **arquitectura hexagonal pura** en Java 21 + Maven.
Stack: Spring Boot (Spring Framework, Spring Data JPA, Hibernate), Oracle PL/SQL, Kafka, API REST (expuesta y consumida) y Quarkus.

## Arquitectura

```
                 ┌──────────────────────────── infrastructure ────────────────────────────┐
  HTTP  ───────► │ REST controllers (in)        JPA/Hibernate adapters (out)   ──► Oracle  │
  Kafka ───────► │ Kafka listener (in)          PL/SQL adapter (out)           ──► Oracle  │
                 │                              Kafka publisher (out)          ──► Kafka   │
                 │                              REST client adapter (out)      ──► API ext. │
                 └───────────────┬───────────────────────────────▲─────────────────────────┘
                                 │ ports.in                       │ ports.out
                          ┌──────▼────────────── application ─────┴───────┐
                          │ Use cases (services): orquestan, no deciden    │
                          └──────────────────────┬─────────────────────────┘
                                                 │
                          ┌──────────────────────▼─────────────────────────┐
                          │ domain: Patient, Appointment, TimeSlot, eventos │
                          │ (reglas de negocio, Java puro, 0 dependencias)  │
                          └─────────────────────────────────────────────────┘
```

| Módulo | Contenido | Dependencias |
|--------|-----------|--------------|
| `domain` | Entidades, value objects, eventos y excepciones de negocio | Ninguna |
| `application` | Puertos de entrada (casos de uso) y de salida, servicios de aplicación | Solo `domain` |
| `infrastructure-spring` | Adaptadores REST, JPA/Hibernate, PL/SQL, Kafka, cliente REST + composición Spring | Spring Boot, Oracle, Kafka |
| `infrastructure-quarkus` | Segundo punto de entrada con Quarkus reutilizando `domain` y `application` con adaptadores en memoria | Quarkus |

Reglas de dependencia (siempre hacia dentro) verificadas con **ArchUnit** en `HexagonalArchitectureTest`.

### Responsabilidades
- **Dominio**: invariantes (cita en el futuro, duración máxima, cancelación solo si está programada).
- **Aplicación**: secuencia del caso de uso (paciente → cobertura → disponibilidad → guardar → evento).
- **Adaptadores**: traducción técnica (mappers dominio↔JPA, DTOs, JSON de Kafka, llamada al procedimiento PL/SQL).
- **Composición** (`UseCaseConfig` / `UseCaseProducers`): único sitio donde se instancian los servicios.

### Decisiones y límites conocidos
- **Transactional Outbox**: los casos de uso que publican eventos se ejecutan en una única transacción (decorador con `TransactionTemplate` en `UseCaseConfig`, así la capa de aplicación sigue libre de Spring). El adaptador `OutboxDomainEventPublisher` guarda el evento en la tabla `outbox_events` junto con el cambio de negocio, y `OutboxRelay` lo reenvía a Kafka cada segundo. Si Kafka está caído, los eventos esperan en la tabla y salen cuando vuelve.
- Entrega **at-least-once**: un evento puede llegar duplicado (caída entre el ack de Kafka y el marcado, o varias réplicas del relay). Los consumidores deben deduplicar con `eventId`. Para varias réplicas, usa `SELECT ... FOR UPDATE SKIP LOCKED` o ShedLock. El módulo Quarkus no usa outbox (publicador en log).
- Seguridad: por defecto se valida un JWT firmado con un secreto compartido (solo desarrollo). En producción elimina `clinic.security.jwt-secret` y configura `spring.security.oauth2.resourceserver.jwt.issuer-uri` con tu proveedor (Keycloak, Entra ID, Auth0). El módulo Quarkus no lleva seguridad.
- `ddl-auto=none`: el esquema y el paquete PL/SQL están en `db/init`.
- La verificación de cobertura usa un stub por defecto (`clinic.coverage.mode=stub`); con `rest` consume `GET {base-url}/coverages/{documento}` → `{"active": true}`.

## Requisitos
Java 21, Maven 3.9+, Docker.

## Ejecutar

```bash
docker compose up -d          # Oracle Free (esquema + PL/SQL) y Kafka
mvn clean verify              # unitarios + ArchUnit + integración con Testcontainers (Oracle y Kafka)
mvn clean verify -DskipITs    # sin los tests de integración (no requiere Docker)
mvn -pl infrastructure-spring -am spring-boot:run     # Spring en :8080
mvn -pl infrastructure-quarkus -am quarkus:dev        # Quarkus en :8081 (en memoria, sin Docker)
```

La primera vez, Oracle tarda 1-2 minutos en crear el esquema. Si cambias los scripts SQL, recrea el contenedor (`docker compose down -v`).

## API

```bash
TOKEN=$(scripts/dev-token.sh)
AUTH="Authorization: Bearer $TOKEN"

curl -X POST localhost:8080/api/patients -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"fullName":"Ana Pérez","documentId":"12345678Z","birthDate":"1990-01-01"}'

curl -X POST localhost:8080/api/appointments -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"patientId":"<id>","doctorId":"<uuid>","startsAt":"2030-01-02T09:00:00Z","endsAt":"2030-01-02T09:30:00Z","reason":"Revisión"}'

curl -X POST -H "$AUTH" localhost:8080/api/appointments/<id>/cancel
curl -H "$AUTH" localhost:8080/api/patients/<id>/appointments
```

| Situación | HTTP |
|-----------|------|
| Datos inválidos | 400 |
| Paciente/cita no existe | 404 |
| Médico ocupado, paciente duplicado, estado inválido | 409 |
| Paciente sin cobertura | 422 |
| Sin token o token inválido | 401 |
| API de cobertura caída (timeout/5xx) | 503 |

Los eventos salen al topic `clinic.appointment.events` (clave = id de la cita).

## Alineado con la demanda laboral en España

Datos del mercado (Tecnoempleo, agosto 2026): Java es la tecnología más demandada (1.336 ofertas), seguida de Python (1.131), SQL (934), AWS (684) y Azure (456). Las ofertas de backend Java piden a menudo Spring Boot, microservicios, Kafka, Oracle/PL-SQL, Angular en full stack (banca/seguros), cloud, Kubernetes y Scrum.

| Lo que piden las ofertas | Dónde está en el proyecto |
|---|---|
| Java + Spring Boot, microservicios | `infrastructure-spring`, arquitectura hexagonal |
| SQL / Oracle / PL-SQL | `db/init`, `PlSqlDoctorAvailabilityAdapter`, JPA/Hibernate |
| Kafka + consistencia de eventos | Patrón Transactional Outbox (`outbox`), listener en `messaging` |
| APIs REST + OpenAPI/Swagger | Controladores, contrato en `/v3/api-docs`, UI en `/swagger-ui.html` |
| Seguridad OAuth2 / JWT | `SecurityConfig` (resource server), `scripts/dev-token.sh` |
| Testing serio (unitario + integración) | JUnit 5, Mockito, ArchUnit y Testcontainers (`AppointmentFlowIT`) |
| Docker y Kubernetes | `Dockerfile`, `k8s/` (probes, recursos, ConfigMap y Secret) |
| CI/CD | `.github/workflows/ci.yml` |
| Observabilidad | Actuator + métricas Prometheus (`/actuator/prometheus`) |
| Resiliencia al consumir APIs | Timeouts y traducción a 503 en el cliente REST |
| Scrum | `docs/AGILE.md` |

Desplegar en AWS o Azure es aplicar `k8s/` sobre EKS/AKS y publicar la imagen en ECR/ACR. Notas: `/actuator/prometheus` requiere token; en un clúster restríngelo con una NetworkPolicy o exponlo en otro puerto de gestión.

## Sector sanitario
Dominio de citas médicas. Pasos naturales de evolución: historia clínica, interoperabilidad HL7 FHIR en un adaptador, y cumplimiento RGPD (datos de salud son categoría especial: cifrado, auditoría de accesos, mínimos privilegios).

## Metodología
Ver [docs/AGILE.md](docs/AGILE.md) (historias, Definition of Done y ceremonias).
