package ar.edu.utn.frvm.prode.prediction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de entrada para crear o modificar un pronostico.
 *
 * El cliente solo informa goles. La tendencia se calcula en el backend para no
 * confiar en datos derivados enviados por el cliente.
 *
 * @param predictedHomeGoals goles pronosticados para el equipo local.
 * @param predictedAwayGoals goles pronosticados para el equipo visitante.
 */
public record PredictionUpsertRequest(
		@NotNull(message = "Los goles pronosticados del local son obligatorios")
		@PositiveOrZero(message = "Los goles pronosticados del local no pueden ser negativos")
		Integer predictedHomeGoals,

		@NotNull(message = "Los goles pronosticados del visitante son obligatorios")
		@PositiveOrZero(message = "Los goles pronosticados del visitante no pueden ser negativos")
		Integer predictedAwayGoals
) {
}
