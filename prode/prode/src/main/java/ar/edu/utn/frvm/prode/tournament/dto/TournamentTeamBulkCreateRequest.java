package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para carga masiva de equipos de un torneo.
 *
 * @param content texto con un equipo por linea.
 */
public record TournamentTeamBulkCreateRequest(
		@NotBlank(message = "La carga masiva no puede estar vacia")
		String content
) {
}
