package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para cambiar el estado de un torneo.
 *
 * @param status nuevo estado solicitado.
 */
public record TournamentStatusUpdateRequest(
		@NotBlank(message = "El estado del torneo es obligatorio")
		String status
) {
}
