package ar.edu.utn.frvm.prode.matchday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatchDayCreateRequest(
		@NotBlank(message = "El nombre de la fecha es obligatorio")
		@Size(max = 120, message = "El nombre de la fecha no puede superar 120 caracteres")
		String name
) {
}
