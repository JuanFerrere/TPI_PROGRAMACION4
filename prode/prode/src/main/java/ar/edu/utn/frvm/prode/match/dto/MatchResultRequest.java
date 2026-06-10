package ar.edu.utn.frvm.prode.match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MatchResultRequest(
		@NotNull(message = "Los goles del equipo local son obligatorios")
		@PositiveOrZero(message = "Los goles del equipo local no pueden ser negativos")
		Integer homeGoals,

		@NotNull(message = "Los goles del equipo visitante son obligatorios")
		@PositiveOrZero(message = "Los goles del equipo visitante no pueden ser negativos")
		Integer awayGoals
) {
}
