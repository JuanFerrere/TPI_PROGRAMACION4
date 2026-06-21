package ar.edu.utn.frvm.prode.tournament.dto;

import java.util.List;

/**
 * Tabla deportiva de un grupo del torneo.
 *
 * En formato LEAGUE hay una sola tabla con groupName null.
 * En formato GROUPS hay una por cada grupo.
 *
 * @param groupName nombre del grupo (null si el torneo es LEAGUE).
 * @param rows filas ya ordenadas por posicion.
 */
public record StandingsGroupResponse(
		String groupName,
		List<StandingRowResponse> rows
) {
}
