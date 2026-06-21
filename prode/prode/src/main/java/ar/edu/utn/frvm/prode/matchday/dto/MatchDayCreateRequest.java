package ar.edu.utn.frvm.prode.matchday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear una fecha o jornada.
 *
 * El cliente solo informa el nombre; el backend asigna el estado PROGRAMADA.
 *
 * @param name nombre obligatorio de la fecha.
 */
public record MatchDayCreateRequest(
		@NotBlank(message = "El nombre de la fecha es obligatorio") // Evita crear fechas sin nombre real.
		@Size(max = 100, message = "El nombre de la fecha no puede superar 100 caracteres") // Mantiene coherencia con la columna name.
		String name
) {
}
