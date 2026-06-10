package ar.edu.utn.frvm.prode.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(
		@NotBlank(message = "El nombre del equipo es obligatorio")
		@Size(max = 100, message = "El nombre del equipo no puede superar 100 caracteres")
		String name
) {
}
