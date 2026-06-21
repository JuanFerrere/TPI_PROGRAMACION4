package ar.edu.utn.frvm.prode.tournament.dto;

import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;

import java.util.List;

/**
 * Respuesta completa de la tabla deportiva de un torneo.
 *
 * @param tournamentId id del torneo.
 * @param tournamentName nombre del torneo.
 * @param format formato del torneo (GROUPS o LEAGUE).
 * @param groups tablas por grupo (una sola en LEAGUE).
 */
public record TournamentStandingsResponse(
		Long tournamentId,
		String tournamentName,
		TournamentFormat format,
		List<StandingsGroupResponse> groups
) {
}
