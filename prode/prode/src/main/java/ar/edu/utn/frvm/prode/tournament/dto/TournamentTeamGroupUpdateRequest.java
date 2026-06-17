package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para cambiar el grupo de una participacion.
 *
 * @param groupName nuevo grupo del equipo dentro del torneo.
 */
public record TournamentTeamGroupUpdateRequest(
		@NotBlank(message = "El grupo es obligatorio")
		@Size(max = 20, message = "El grupo no puede superar 20 caracteres")
		String groupName
) {
}
