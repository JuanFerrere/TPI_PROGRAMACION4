package ar.edu.utn.frvm.prode.tournament.entity;

/**
 * Estados posibles de un torneo.
 *
 * En este primer paso no se aplican transiciones complejas; solo se valida que
 * el valor recibido pertenezca a este enum.
 */
public enum TournamentStatus {

	DRAFT,
	ACTIVE,
	FINISHED,
	ARCHIVED
}
