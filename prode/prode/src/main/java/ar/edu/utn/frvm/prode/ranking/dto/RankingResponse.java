package ar.edu.utn.frvm.prode.ranking.dto;

/**
 * DTO de salida para una fila del ranking global.
 *
 * Cada fila representa a un usuario con sus puntos acumulados a lo largo de todos
 * sus pronosticos. La posicion se calcula en el backend luego de ordenar.
 *
 * @param position posicion en la tabla, empezando en 1 (1 es el lider).
 * @param userId id del usuario.
 * @param username nombre publico del usuario.
 * @param totalPoints suma de puntos de todos sus pronosticos.
 * @param exactHits cantidad de pronosticos con acierto exacto.
 * @param predictionsCount cantidad total de pronosticos cargados por el usuario.
 */
public record RankingResponse(
		Integer position,
		Long userId,
		String username,
		Integer totalPoints,
		Long exactHits,
		Long predictionsCount
) {
}
