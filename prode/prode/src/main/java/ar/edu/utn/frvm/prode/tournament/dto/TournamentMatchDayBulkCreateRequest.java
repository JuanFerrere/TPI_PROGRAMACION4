package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO de entrada para crear varias fechas dentro de un torneo.
 *
 * @param matchDays fechas a crear.
 */
public record TournamentMatchDayBulkCreateRequest(
		@NotEmpty(message = "La lista de fechas no puede estar vacia")
		List<@Valid TournamentMatchDayCreateRequest> matchDays
) {
}
