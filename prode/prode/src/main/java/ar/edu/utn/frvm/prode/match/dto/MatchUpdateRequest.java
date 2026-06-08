package ar.edu.utn.frvm.prode.match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * DTO de entrada para modificar un partido.
 *
 * PUT usa este DTO completo: local, visitante y horario deben venir informados.
 *
 * @param homeTeamId nuevo id obligatorio del equipo local.
 * @param awayTeamId nuevo id obligatorio del equipo visitante.
 * @param startTime nuevo instante UTC obligatorio de inicio.
 */
public record MatchUpdateRequest(
		@NotNull(message = "El equipo local es obligatorio") // Evita dejar el partido sin local.
		@Positive(message = "El id del equipo local debe ser un numero positivo")
		Long homeTeamId,

		@NotNull(message = "El equipo visitante es obligatorio") // Evita dejar el partido sin visitante.
		@Positive(message = "El id del equipo visitante debe ser un numero positivo")
		Long awayTeamId,

		@NotNull(message = "El horario de inicio es obligatorio") // El horario debe mantenerse definido.
		Instant startTime
) {
}
