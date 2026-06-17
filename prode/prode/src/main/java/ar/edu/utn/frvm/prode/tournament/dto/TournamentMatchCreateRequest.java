package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * DTO de entrada para crear un partido dentro de un torneo.
 *
 * @param matchDayId id de la fecha del torneo.
 * @param homeTournamentTeamId participacion del local dentro del torneo.
 * @param awayTournamentTeamId participacion del visitante dentro del torneo.
 * @param startTime horario de inicio.
 */
public record TournamentMatchCreateRequest(
		@NotNull(message = "La fecha del partido es obligatoria")
		@Positive(message = "El id de la fecha debe ser positivo")
		Long matchDayId,

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
