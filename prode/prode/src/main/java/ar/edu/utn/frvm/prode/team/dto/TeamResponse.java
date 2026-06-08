package ar.edu.utn.frvm.prode.team.dto;

import java.time.Instant;

/**
 * DTO de salida para devolver equipos al cliente.
 *
 * No expone entidades JPA directamente, solo los datos necesarios para la API.
 *
 * @param id identificador del equipo.
 * @param name nombre del equipo.
 * @param createdAt instante de creacion en UTC. Instant evita depender de la zona horaria del servidor.
 */
public record TeamResponse(
		Long id,
		String name,
		Instant createdAt
) {
}
