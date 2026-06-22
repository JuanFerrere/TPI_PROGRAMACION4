package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Request para avanzar la llave eliminatoria a la siguiente ronda.
 *
 * @param nextRoundStartTime horario base para los partidos de la nueva ronda.
 */
public record KnockoutAdvanceRequest(
		@NotNull(message = "El horario de la siguiente ronda es obligatorio")
		Instant nextRoundStartTime
) {
}
