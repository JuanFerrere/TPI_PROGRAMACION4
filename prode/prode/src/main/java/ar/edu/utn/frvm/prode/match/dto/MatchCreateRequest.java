package ar.edu.utn.frvm.prode.match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record MatchCreateRequest(
		@NotNull(message = "La fecha del partido es obligatoria")
		@Positive(message = "El id de la fecha debe ser un numero positivo")
		Long matchDayId,

		@NotNull(message = "El equipo local es obligatorio")
		@Positive(message = "El id del equipo local debe ser un numero positivo")
		Long homeTeamId,

		@NotNull(message = "El equipo visitante es obligatorio")
		@Positive(message = "El id del equipo visitante debe ser un numero positivo")
		Long awayTeamId,

		@NotNull(message = "El horario de inicio es obligatorio")
		Instant startTime
) {
}
