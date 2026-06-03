package ar.edu.utn.frvm.prode.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa los datos enviados por el cliente para iniciar sesion.
 *
 * @param usernameOrEmail permite ingresar con username o email.
 * @param password contrasena ingresada por el usuario.
 */
public record LoginRequest(
		@NotBlank(message = "El username o email es obligatorio") // El login necesita identificar al usuario.
		String usernameOrEmail,

		@NotBlank(message = "La contrasena es obligatoria") // Sin contrasena no se puede autenticar.
		String password
) {
}
