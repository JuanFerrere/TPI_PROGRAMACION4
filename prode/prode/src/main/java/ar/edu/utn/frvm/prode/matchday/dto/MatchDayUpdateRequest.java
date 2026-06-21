package ar.edu.utn.frvm.prode.matchday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para modificar una fecha o jornada.
 *
 * El cliente no puede modificar el status; el backend lo recalcula segun partidos.
 *
 * @param name nuevo nombre obligatorio de la fecha.
 */
public record MatchDayUpdateRequest(
		@NotBlank(message = "El nombre de la fecha es obligatorio") // Evita reemplazar el nombre por texto vacio.
		@Size(max = 100, message = "El nombre de la fecha no puede superar 100 caracteres") // Respeta el limite de la entidad.
		String name
) {
}
