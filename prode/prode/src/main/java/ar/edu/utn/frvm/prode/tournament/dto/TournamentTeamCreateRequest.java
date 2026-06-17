package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para agregar un equipo a un torneo.
 *
 * @param name nombre del equipo.
 * @param groupName grupo opcional segun formato del torneo.
 */
public record TournamentTeamCreateRequest(
		@NotBlank(message = "El nombre del equipo es obligatorio")
		@Size(max = 100, message = "El nombre del equipo no puede superar 100 caracteres")
		String name,

		@Size(max = 20, message = "El grupo no puede superar 20 caracteres")
		String groupName
) {
}
