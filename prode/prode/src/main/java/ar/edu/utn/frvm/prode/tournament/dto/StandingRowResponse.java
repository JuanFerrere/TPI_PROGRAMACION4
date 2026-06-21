package ar.edu.utn.frvm.prode.tournament.dto;

/**
 * Fila de la tabla deportiva: estadisticas acumuladas de un equipo en el torneo.
 *
 * @param position posicion dentro de su tabla (1 = puntero).
 * @param teamId id del equipo.
 * @param teamName nombre del equipo.
 * @param groupName grupo al que pertenece (null en formato LEAGUE).
 * @param played partidos jugados (PJ).
 * @param won partidos ganados (G).
 * @param drawn partidos empatados (E).
 * @param lost partidos perdidos (P).
 * @param goalsFor goles a favor (GF).
 * @param goalsAgainst goles en contra (GC).
 * @param goalDifference diferencia de gol (DG).
 * @param points puntos (3 por victoria, 1 por empate).
 */
public record StandingRowResponse(
		Integer position,
		Long teamId,
		String teamName,
		String groupName,
		Integer played,
		Integer won,
		Integer drawn,
		Integer lost,
		Integer goalsFor,
		Integer goalsAgainst,
		Integer goalDifference,
		Integer points
) {
}
