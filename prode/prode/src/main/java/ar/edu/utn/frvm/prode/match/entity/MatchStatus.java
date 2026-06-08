package ar.edu.utn.frvm.prode.match.entity;

/**
 * Enum que representa el estado de un partido individual.
 *
 * Este estado se usa para controlar que solo se modifiquen o eliminen partidos que todavia no empezaron.
 */
public enum MatchStatus {
	/**
	 * Estado inicial: el partido todavia no fue iniciado.
	 */
	POR_JUGARSE,

	/**
	 * Partido iniciado por un usuario con rol ADMIN.
	 */
	EN_JUEGO,

	/**
	 * Partido terminado. Se usara en la etapa de carga de resultados reales.
	 */
	FINALIZADO
}
