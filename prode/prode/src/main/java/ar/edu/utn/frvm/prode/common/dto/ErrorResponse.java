package ar.edu.utn.frvm.prode.common.dto;

import java.time.Instant;

/**
 * DTO comun para devolver errores de la API con una estructura uniforme.
 *
 * @param timestamp momento exacto en que ocurrio el error.
 * @param status codigo HTTP numerico.
 * @param error nombre del error HTTP.
 * @param message mensaje claro para el cliente.
 * @param path endpoint donde ocurrio el error.
 */
public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path
) {
}
