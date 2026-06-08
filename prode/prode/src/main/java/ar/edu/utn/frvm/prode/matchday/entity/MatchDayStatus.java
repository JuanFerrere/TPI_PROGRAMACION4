package ar.edu.utn.frvm.prode.matchday.entity;

/**
 * Enum que representa el estado general de una fecha o jornada.
 *
 * El backend recalcula este estado segun los partidos asociados; el cliente no lo edita libremente.
 */
public enum MatchDayStatus {
	/**
	 * Estado inicial de una fecha. Todos sus partidos estan pendientes o todavia no tiene partidos cargados.
	 */
	PROGRAMADA,

	/**
	 * Al menos un partido de la fecha esta en juego.
	 */
	EN_JUEGO,

	/**
	 * Todos los partidos de la fecha terminaron.
	 */
	FINALIZADA
}
