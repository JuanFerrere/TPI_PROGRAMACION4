package ar.edu.utn.frvm.prode.match.dto;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;

import java.time.Instant;

public record MatchResponse(
		Long id,
		Long matchDayId,
		String matchDayName,
		Long homeTeamId,
		String homeTeamName,
		Long awayTeamId,
		String awayTeamName,
		Instant startTime,
		MatchStatus status,
		Integer homeGoals,
		Integer awayGoals,
		ResultTrend resultTrend,
		Instant createdAt,
		Instant updatedAt
) {
}
