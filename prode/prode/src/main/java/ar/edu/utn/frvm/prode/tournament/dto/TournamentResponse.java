package ar.edu.utn.frvm.prode.tournament.dto;

import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;

import java.time.Instant;

/**
 * DTO de salida para devolver torneos al cliente.
 *
 * @param id identificador del torneo.
 * @param name nombre del torneo.
 * @param description descripcion opcional.
 * @param status estado actual.
 * @param format formato deportivo.
 * @param createdAt instante de creacion en UTC.
 * @param updatedAt instante de ultima modificacion en UTC.
 */
public record TournamentResponse(
		Long id,
		String name,
		String description,
		TournamentStatus status,
		TournamentFormat format,
		Instant createdAt,
		Instant updatedAt
) {
}
