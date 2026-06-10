package ar.edu.utn.frvm.prode.prediction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PredictionUpsertRequest(
		@NotNull(message = "Los goles pronosticados del local son obligatorios")
		@PositiveOrZero(message = "Los goles pronosticados del local no pueden ser negativos")
		Integer predictedHomeGoals,

		@NotNull(message = "Los goles pronosticados del visitante son obligatorios")
		@PositiveOrZero(message = "Los goles pronosticados del visitante no pueden ser negativos")
		Integer predictedAwayGoals
) {
}
