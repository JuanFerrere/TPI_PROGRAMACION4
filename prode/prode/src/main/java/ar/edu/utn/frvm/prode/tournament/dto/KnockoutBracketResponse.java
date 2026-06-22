package ar.edu.utn.frvm.prode.tournament.dto;

import java.util.List;

/**
 * Respuesta completa de la llave eliminatoria existente de un torneo.
 *
 * @param tournamentId id del torneo.
 * @param tournamentName nombre del torneo.
 * @param rounds rondas eliminatorias agrupadas.
 */
public record KnockoutBracketResponse(
		Long tournamentId,
		String tournamentName,
		List<KnockoutRoundResponse> rounds
) {
}
