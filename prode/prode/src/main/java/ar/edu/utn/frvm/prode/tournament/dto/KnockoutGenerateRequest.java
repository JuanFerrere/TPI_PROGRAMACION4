package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Request para generar la primera ronda eliminatoria de un torneo.
 *
 * @param qualifiersCount cantidad total de clasificados a la llave.
 * @param qualifiedPerGroup cantidad de clasificados por grupo, obligatoria en formato GROUPS.
 * @param firstRoundStartTime horario base para los partidos de la primera ronda.
 */
public record KnockoutGenerateRequest(
		@NotNull(message = "La cantidad de clasificados es obligatoria")
		Integer qualifiersCount,

		Integer qualifiedPerGroup,

		@NotNull(message = "El horario de la primera ronda es obligatorio")
		Instant firstRoundStartTime
) {
}
