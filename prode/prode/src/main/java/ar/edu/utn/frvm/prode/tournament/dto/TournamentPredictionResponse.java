package ar.edu.utn.frvm.prode.tournament.dto;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;

import java.time.Instant;

/**
 * DTO de salida para pronosticos filtrados por torneo.
 */
public record TournamentPredictionResponse(
		Long id,
		Long tournamentId,
		String tournamentName,
		Long userId,
		String username,
		Long matchId,
		String matchDayName,
		String homeTeamName,
		String awayTeamName,
		Instant matchStartTime,
		MatchStatus matchStatus,
		Integer predictedHomeGoals,
		Integer predictedAwayGoals,
		ResultTrend predictedTrend,
		Integer homeGoals,
		Integer awayGoals,
		ResultTrend resultTrend,
		Integer points,
		Boolean exactHit,
		Instant createdAt,
		Instant updatedAt
) {
}
