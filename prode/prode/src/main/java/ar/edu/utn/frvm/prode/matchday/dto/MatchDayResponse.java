package ar.edu.utn.frvm.prode.matchday.dto;

import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;

import java.time.Instant;

public record MatchDayResponse(
		Long id,
		String name,
		MatchDayStatus status,
		Instant createdAt
) {
}
