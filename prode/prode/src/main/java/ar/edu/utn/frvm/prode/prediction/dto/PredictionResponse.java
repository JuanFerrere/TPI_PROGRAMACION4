package ar.edu.utn.frvm.prode.prediction.dto;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;

import java.time.Instant;

public record PredictionResponse(
		Long id,
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
		Integer points,
		Boolean exactHit,
		Instant createdAt,
		Instant updatedAt
) {
}
