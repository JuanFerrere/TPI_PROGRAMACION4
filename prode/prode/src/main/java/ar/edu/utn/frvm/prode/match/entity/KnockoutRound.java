package ar.edu.utn.frvm.prode.match.entity;

/**
 * Ronda eliminatoria asociada a un partido KNOCKOUT.
 *
 * Solo se usa cuando Match.phase = KNOCKOUT.
 * Puede quedar null para partidos REGULAR.
 */
public enum KnockoutRound {

	ROUND_OF_16,

	QUARTER_FINAL,

	SEMI_FINAL,

	FINAL
}
