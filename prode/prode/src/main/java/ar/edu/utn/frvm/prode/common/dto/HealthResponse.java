package ar.edu.utn.frvm.prode.common.dto;

/**
 * DTO usado por el endpoint tecnico de salud de la API.
 *
 * @param status  indica el estado general de la aplicacion.
 * @param message mensaje simple para confirmar que la API responde.
 */
public record HealthResponse(
		String status,
		String message
) {
}
