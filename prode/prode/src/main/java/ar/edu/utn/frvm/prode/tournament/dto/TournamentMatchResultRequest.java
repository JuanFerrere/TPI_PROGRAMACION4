package ar.edu.utn.frvm.prode.tournament.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TournamentMatchResultRequest(
        @NotNull(message = "Los goles del equipo local son obligatorios")
        @PositiveOrZero(message = "Los goles del equipo local no pueden ser negativos")
        Integer homeGoals,

        @NotNull(message = "Los goles del equipo visitante son obligatorios")
        @PositiveOrZero(message = "Los goles del equipo visitante no pueden ser negativos")
        Integer awayGoals,

        @PositiveOrZero(message = "Los penales del equipo local no pueden ser negativos")
        Integer homePenaltyGoals,

        @PositiveOrZero(message = "Los penales del equipo visitante no pueden ser negativos")
        Integer awayPenaltyGoals
) {

    public TournamentMatchResultRequest(
            Integer homeGoals,
            Integer awayGoals
    ) {
        this(homeGoals, awayGoals, null, null);
    }
}