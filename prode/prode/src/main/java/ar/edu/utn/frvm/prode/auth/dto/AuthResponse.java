package ar.edu.utn.frvm.prode.auth.dto;

import ar.edu.utn.frvm.prode.user.entity.Role;

/**
 * DTO devuelto luego de un registro o login exitoso.
 *
 * No incluye password porque la contrasena nunca debe devolverse al cliente.
 *
 * @param token JWT generado para autenticar futuras peticiones.
 * @param tokenType tipo de token. Para JWT por header se usa Bearer.
 * @param username nombre de usuario autenticado.
 * @param email email del usuario autenticado.
 * @param role rol del usuario autenticado.
 */
public record AuthResponse(
		String token,
		String tokenType,
		String username,
		String email,
		Role role
) {
}
