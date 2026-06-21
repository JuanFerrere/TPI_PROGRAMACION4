package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para cambiar el formato de un torneo.
 *
 * @param format nuevo formato solicitado.
 */
public record TournamentFormatUpdateRequest(
		@NotBlank(message = "El formato del torneo es obligatorio")
		String format
) {
}
