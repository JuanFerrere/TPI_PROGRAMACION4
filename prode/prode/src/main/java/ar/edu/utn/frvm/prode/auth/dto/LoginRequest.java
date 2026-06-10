package ar.edu.utn.frvm.prode.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "El username o email es obligatorio")
		String usernameOrEmail,

		@NotBlank(message = "La contrasena es obligatoria")
		String password
) {
}
