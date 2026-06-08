package ar.edu.utn.frvm.prode.match.entity;

/**
 * Enum que representa la tendencia del resultado de un partido.
 *
 * En etapas futuras servira para representar el resultado real o la tendencia de resultado:
 * gana local, empate o gana visitante.
 */
public enum ResultTrend {
	/**
	 * Indica que gana el equipo local.
	 */
	LOCAL,

	/**
	 * Indica que el partido termina empatado.
	 */
	EMPATE,

	/**
	 * Indica que gana el equipo visitante.
	 */
	VISITANTE
}
