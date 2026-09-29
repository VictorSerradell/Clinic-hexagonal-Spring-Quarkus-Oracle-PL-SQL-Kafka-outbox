# Trabajo en equipo ágil (Scrum) — cómo encaja este proyecto

## Historias de usuario del MVP

| # | Historia | Criterios de aceptación (resumen) |
|---|----------|-----------------------------------|
| US-1 | Como recepcionista quiero registrar un paciente para poder darle citas | DNI/NIE único; nombre y fecha de nacimiento obligatorios; 409 si ya existe |
| US-2 | Como recepcionista quiero dar una cita a un paciente con un médico | Solo con cobertura activa; sin solapes para el médico; publica evento `AppointmentScheduled` |
| US-3 | Como paciente quiero cancelar mi cita | Solo si está `SCHEDULED` y no ha empezado; publica `AppointmentCancelled` |
| US-4 | Como recepcionista quiero ver las citas de un paciente | Ordenadas por fecha; 404 si el paciente no existe |

## Definition of Done

- Código revisado por otra persona (pull request).
- Tests unitarios en verde (`mvn verify`), incluido el test de arquitectura (ArchUnit).
- Sin dependencias de framework en `domain` ni `application`.
- Scripts SQL/PL/SQL versionados en `db/init`.
- README/docs actualizados si cambia la API o la arquitectura.

## Ceremonias sugeridas
Planning (historias estimadas en puntos), daily de 15 min, review con demo de los endpoints y retrospectiva por sprint.
