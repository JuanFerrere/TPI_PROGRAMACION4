package ar.edu.utn.frvm.prode.tournament.dto;

import java.time.Instant;

/**
 * DTO de salida para equipos asociados a un torneo.
 *
 * @param id identificador de la participacion.
 * @param tournamentId identificador del torneo.
 * @param teamId identificador del equipo global.
 * @param teamName nombre visible del equipo.
 * @param groupName grupo dentro del torneo, si aplica.
 * @param createdAt fecha de asociacion.
 * @param updatedAt fecha de ultima modificacion.
 */
public record TournamentTeamResponse(
		Long id,
		Long tournamentId,
		Long teamId,
		String teamName,
		String groupName,
		Instant createdAt,
		Instant updatedAt
) {
}
