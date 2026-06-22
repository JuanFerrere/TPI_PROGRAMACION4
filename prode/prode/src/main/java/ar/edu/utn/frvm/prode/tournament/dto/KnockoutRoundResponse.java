package ar.edu.utn.frvm.prode.tournament.dto;

import ar.edu.utn.frvm.prode.match.entity.KnockoutRound;

import java.util.List;

/**
 * Ronda dentro de una llave eliminatoria.
 *
 * @param round codigo de la ronda.
 * @param label texto legible para mostrar la ronda.
 * @param matches partidos de la ronda ordenados por posicion de llave.
 */
public record KnockoutRoundResponse(
		KnockoutRound round,
		String label,
		List<KnockoutMatchResponse> matches
) {
}
