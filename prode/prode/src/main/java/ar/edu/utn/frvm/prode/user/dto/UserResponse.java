package ar.edu.utn.frvm.prode.user.dto;

import ar.edu.utn.frvm.prode.user.entity.Role;

import java.time.Instant;

/**
 * DTO publico para devolver datos seguros de un usuario.
 *
 * No incluye password porque ni siquiera el hash debe exponerse en respuestas HTTP.
 *
 * @param id identificador del usuario.
 * @param username nombre de usuario.
 * @param email correo electronico.
 * @param role rol asignado.
 * @param createdAt fecha de creacion.
 */
public record UserResponse(
		Long id,
		String username,
		String email,
		Role role,
		Instant createdAt
) {
}
