package ar.edu.utn.frvm.prode.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear equipos.
 *
 * El cliente envia este objeto en el body de POST /api/teams.
 *
 * @param name nombre obligatorio del equipo.
 */
public record TeamCreateRequest(
		@NotBlank(message = "El nombre del equipo es obligatorio") // Evita null, texto vacio o solo espacios.
		@Size(max = 100, message = "El nombre del equipo no puede superar 100 caracteres") // Coincide con el limite de la entidad.
		String name
) {
}
