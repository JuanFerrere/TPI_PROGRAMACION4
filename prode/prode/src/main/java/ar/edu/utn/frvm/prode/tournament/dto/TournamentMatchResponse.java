package ar.edu.utn.frvm.prode.tournament.dto;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;

import java.time.Instant;

/**
 * DTO de salida para partidos de torneo.
 *
 * @param id identificador del partido.
 * @param tournamentId identificador del torneo.
 * @param matchDayId identificador de la fecha.
 * @param matchDayName nombre de la fecha.
 * @param homeTeamId id global del equipo local.
 * @param homeTournamentTeamId id de participacion del local.
 * @param homeTeamName nombre del local.
 * @param homeGroupName grupo del local, si aplica.
 * @param awayTeamId id global del visitante.
 * @param awayTournamentTeamId id de participacion del visitante.
 * @param awayTeamName nombre del visitante.
 * @param awayGroupName grupo del visitante, si aplica.
 * @param startTime horario de inicio.
 * @param status estado actual.
 */
public record TournamentMatchResponse(
		Long id,
		Long tournamentId,
		Long matchDayId,
		String matchDayName,
		Long homeTeamId,
		Long homeTournamentTeamId,
		String homeTeamName,
		String homeGroupName,
		Long awayTeamId,
		Long awayTournamentTeamId,
		String awayTeamName,
		String awayGroupName,
		Instant startTime,
		MatchStatus status
) {
}
