package ar.edu.utn.frvm.prode.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO que representa los datos enviados por el cliente para registrar un usuario.
 *
 * Un DTO transporta datos entre el cliente y la API sin exponer directamente las entidades.
 *
 * @param username nombre de usuario requerido.
 * @param email email requerido y con formato valido.
 * @param password contrasena requerida con minimo 8 caracteres.
 */
public record RegisterRequest(
		@NotBlank(message = "El username es obligatorio") // Valida que no sea null, vacio ni solo espacios.
		String username,

		@NotBlank(message = "El email es obligatorio") // Valida que el email venga informado.
		@Email(message = "El email debe tener un formato valido") // Valida formato basico de correo electronico.
		String email,

		@NotBlank(message = "La contrasena es obligatoria") // Valida que la contrasena venga informada.
		@Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres") // Exige longitud minima.
		String password
) {
}
