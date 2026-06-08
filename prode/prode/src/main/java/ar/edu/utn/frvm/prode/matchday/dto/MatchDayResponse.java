package ar.edu.utn.frvm.prode.matchday.dto;

import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;

import java.time.Instant;

/**
 * DTO de salida para devolver una fecha o jornada.
 *
 * No incluye lista de partidos para evitar respuestas grandes o recursividad JSON.
 *
 * @param id identificador de la fecha.
 * @param name nombre visible de la fecha.
 * @param status estado calculado por el backend.
 * @param createdAt instante de creacion en UTC. Instant representa un punto exacto de tiempo.
 */
public record MatchDayResponse(
		Long id,
		String name,
		MatchDayStatus status,
		Instant createdAt
) {
}
