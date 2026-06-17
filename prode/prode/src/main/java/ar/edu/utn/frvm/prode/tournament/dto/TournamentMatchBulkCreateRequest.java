package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * DTO de entrada para crear varios partidos en una fecha.
 *
 * @param matchDayId id de la fecha del torneo.
 * @param matches partidos a crear.
 */
public record TournamentMatchBulkCreateRequest(
		@NotNull(message = "La fecha del partido es obligatoria")
		@Positive(message = "El id de la fecha debe ser positivo")
		Long matchDayId,

		@NotEmpty(message = "La lista de partidos no puede estar vacia")
		List<@Valid TournamentMatchBulkItemRequest> matches
) {
}
