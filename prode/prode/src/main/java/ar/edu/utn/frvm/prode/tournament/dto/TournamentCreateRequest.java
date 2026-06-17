package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear torneos.
 *
 * @param name nombre obligatorio del torneo.
 * @param description descripcion opcional del torneo.
 * @param format formato deportivo del torneo.
 */
public record TournamentCreateRequest(
		@NotBlank(message = "El nombre del torneo es obligatorio")
		@Size(max = 120, message = "El nombre del torneo no puede superar 120 caracteres")
		String name,

		@Size(max = 500, message = "La descripcion del torneo no puede superar 500 caracteres")
		String description,

		@NotBlank(message = "El formato del torneo es obligatorio")
		String format
) {
}
