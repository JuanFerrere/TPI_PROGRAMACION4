package ar.edu.utn.frvm.prode.prediction.dto;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;

import java.time.Instant;

/**
 * DTO de salida para devolver pronosticos al cliente.
 *
 * Incluye datos publicos del usuario y del partido, pero nunca devuelve
 * password ni informacion sensible.
 *
 * @param id identificador del pronostico.
 * @param userId id del usuario que pronostico.
 * @param username nombre publico del usuario.
 * @param matchId id del partido pronosticado.
 * @param matchDayName nombre de la fecha del partido.
 * @param homeTeamName nombre del equipo local.
 * @param awayTeamName nombre del equipo visitante.
 * @param matchStartTime horario del partido en UTC.
 * @param matchStatus estado actual del partido.
 * @param predictedHomeGoals goles pronosticados para el local.
 * @param predictedAwayGoals goles pronosticados para el visitante.
 * @param predictedTrend tendencia calculada del pronostico.
 * @param points puntos obtenidos; queda en 0 hasta Etapa 5.
 * @param exactHit indica acierto exacto; queda false hasta Etapa 5.
 * @param createdAt instante de creacion del pronostico.
 * @param updatedAt instante de ultima modificacion del pronostico.
 */
public record PredictionResponse(
		Long id,
		Long userId,
		String username,
		Long matchId,
		String matchDayName,
		String homeTeamName,
		String awayTeamName,
		Instant matchStartTime,
		MatchStatus matchStatus,
		Integer predictedHomeGoals,
		Integer predictedAwayGoals,
		ResultTrend predictedTrend,
		Integer points,
		Boolean exactHit,
		Instant createdAt,
		Instant updatedAt
) {
}
