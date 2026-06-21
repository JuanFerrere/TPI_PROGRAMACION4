package ar.edu.utn.frvm.prode.tournament.dto;

import java.time.Instant;

/**
 * DTO de salida para fechas de torneo.
 *
 * @param id identificador de la fecha.
 * @param tournamentId identificador del torneo.
 * @param name nombre de la fecha.
 * @param orderNumber numero de orden.
 * @param createdAt fecha de creacion.
 * @param updatedAt fecha de ultima modificacion.
 */
public record TournamentMatchDayResponse(
		Long id,
		Long tournamentId,
		String name,
		Integer orderNumber,
		Instant createdAt,
		Instant updatedAt
) {
}
