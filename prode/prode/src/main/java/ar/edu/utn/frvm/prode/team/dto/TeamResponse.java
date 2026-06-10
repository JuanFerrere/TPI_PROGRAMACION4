package ar.edu.utn.frvm.prode.team.dto;

import java.time.Instant;

public record TeamResponse(
		Long id,
		String name,
		Instant createdAt
) {
}
