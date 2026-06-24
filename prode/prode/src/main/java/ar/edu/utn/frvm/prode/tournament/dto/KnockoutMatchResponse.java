package ar.edu.utn.frvm.prode.tournament.dto;

import java.time.Instant;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;

/**
 * Partido dentro de una ronda eliminatoria.
 *
 * @param matchId id del partido.
 * @param bracketPosition posicion del partido en la ronda.
 * @param homeTeamId id del equipo local.
 * @param homeTeamName nombre del equipo local.
 * @param awayTeamId id del equipo visitante.
 * @param awayTeamName nombre del equipo visitante.
 * @param startTime horario de inicio.
 * @param status estado del partido.
 * @param homeGoals goles del local, si ya existe resultado.
 * @param awayGoals goles del visitante, si ya existe resultado.
 * @param resultTrend tendencia real, si ya existe resultado.
 * @param winnerTeamId id del ganador, si el partido eliminatorio ya finalizo.
 * @param winnerTeamName nombre del ganador, si el partido eliminatorio ya
 * finalizo.
 */
public record KnockoutMatchResponse(
        Long matchId,
        Integer bracketPosition,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        Instant startTime,
        MatchStatus status,
        Integer homeGoals,
        Integer awayGoals,
        Integer homePenaltyGoals,
        Integer awayPenaltyGoals,
        ResultTrend resultTrend,
        Long winnerTeamId,
        String winnerTeamName
        ) {

}
