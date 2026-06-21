package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear una fecha dentro de un torneo.
 *
 * @param name nombre de la fecha.
 * @param orderNumber numero de orden opcional.
 */
public record TournamentMatchDayCreateRequest(
		@NotBlank(message = "El nombre de la fecha es obligatorio")
		@Size(max = 100, message = "El nombre de la fecha no puede superar 100 caracteres")
		String name,

		@Positive(message = "El numero de orden debe ser positivo")
		Integer orderNumber
) {
}
