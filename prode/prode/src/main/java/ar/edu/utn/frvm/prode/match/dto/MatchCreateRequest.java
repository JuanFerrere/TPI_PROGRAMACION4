package ar.edu.utn.frvm.prode.match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * DTO de entrada para crear un partido.
 *
 * El horario se recibe como ISO-8601 en UTC, por ejemplo: 2026-06-20T19:00:00Z.
 *
 * @param matchDayId id obligatorio de la fecha a la que pertenece el partido.
 * @param homeTeamId id obligatorio del equipo local.
 * @param awayTeamId id obligatorio del equipo visitante.
 * @param startTime instante UTC obligatorio de inicio del partido.
 */
public record MatchCreateRequest(
		@NotNull(message = "La fecha del partido es obligatoria") // Sin fecha no se puede agrupar el partido.
		@Positive(message = "El id de la fecha debe ser un numero positivo")
		Long matchDayId,

		@NotNull(message = "El equipo local es obligatorio") // Todo partido debe tener local.
		@Positive(message = "El id del equipo local debe ser un numero positivo")
		Long homeTeamId,

		@NotNull(message = "El equipo visitante es obligatorio") // Todo partido debe tener visitante.
		@Positive(message = "El id del equipo visitante debe ser un numero positivo")
		Long awayTeamId,

		@NotNull(message = "El horario de inicio es obligatorio") // El horario permite ordenar y bloquear acciones futuras.
		Instant startTime
) {
}
