package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * Item de entrada para crear partidos de forma masiva.
 *
 * @param homeTournamentTeamId participacion del local.
 * @param awayTournamentTeamId participacion del visitante.
 * @param startTime horario de inicio.
 */
public record TournamentMatchBulkItemRequest(
		@NotNull(message = "El equipo local es obligatorio")
		@Positive(message = "El id del equipo local debe ser positivo")
		Long homeTournamentTeamId,

		@NotNull(message = "El equipo visitante es obligatorio")
		@Positive(message = "El id del equipo visitante debe ser positivo")
		Long awayTournamentTeamId,

		@NotNull(message = "El horario de inicio es obligatorio")
		Instant startTime
) {
}
