package ar.edu.utn.frvm.prode.match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de entrada para cargar el resultado real de un partido.
 *
 * El cliente (ADMIN) solo envia los goles reales del local y del visitante.
 * El backend NO confia en ningun otro dato calculado por el cliente: la
 * tendencia (resultTrend), el nuevo estado del partido y los puntos de los
 * pronosticos se calculan siempre en el servidor.
 *
 * @param homeGoals goles reales del equipo local; obligatorio y no negativo.
 * @param awayGoals goles reales del equipo visitante; obligatorio y no negativo.
 */
public record MatchResultRequest(
		@NotNull(message = "Los goles del equipo local son obligatorios") // Sin este dato no se puede cerrar el partido.
		@PositiveOrZero(message = "Los goles del equipo local no pueden ser negativos") // Un marcador no puede ser negativo.
		Integer homeGoals,

		@NotNull(message = "Los goles del equipo visitante son obligatorios") // Sin este dato no se puede cerrar el partido.
		@PositiveOrZero(message = "Los goles del equipo visitante no pueden ser negativos") // Un marcador no puede ser negativo.
		Integer awayGoals
) {
}
