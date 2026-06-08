package ar.edu.utn.frvm.prode.match.dto;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;

import java.time.Instant;

/**
 * DTO de salida para devolver partidos al cliente.
 *
 * Incluye ids y nombres para que el frontend o Insomnia no necesiten hacer consultas extra simples.
 *
 * @param id identificador del partido.
 * @param matchDayId id de la fecha contenedora.
 * @param matchDayName nombre de la fecha contenedora.
 * @param homeTeamId id del equipo local.
 * @param homeTeamName nombre del equipo local.
 * @param awayTeamId id del equipo visitante.
 * @param awayTeamName nombre del equipo visitante.
 * @param startTime instante UTC de inicio del partido.
 * @param status estado actual del partido.
 * @param homeGoals goles del local; puede ser null en esta etapa.
 * @param awayGoals goles del visitante; puede ser null en esta etapa.
 * @param resultTrend tendencia o resultado; puede ser null en esta etapa.
 * @param createdAt instante UTC de creacion.
 * @param updatedAt instante UTC de ultima modificacion.
 */
public record MatchResponse(
		Long id,
		Long matchDayId,
		String matchDayName,
		Long homeTeamId,
		String homeTeamName,
		Long awayTeamId,
		String awayTeamName,
		Instant startTime,
		MatchStatus status,
		Integer homeGoals,
		Integer awayGoals,
		ResultTrend resultTrend,
		Instant createdAt,
		Instant updatedAt
) {
}
