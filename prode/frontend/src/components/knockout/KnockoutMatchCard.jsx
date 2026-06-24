import Badge from "../ui/Badge.jsx";
import Button from "../ui/Button.jsx";
import Card from "../ui/Card.jsx";

const statusVariant = {
  POR_JUGARSE: "primary",
  EN_JUEGO: "amber",
  FINALIZADO: "success",
};

function formatearFecha(fecha) {
  if (!fecha) {
    return "Fecha no disponible";
  }

  const date = new Date(fecha);

  if (Number.isNaN(date.getTime())) {
    return "Fecha no disponible";
  }

  return date.toLocaleString("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function tieneResultado(match) {
  return (
    match.homeGoals !== null &&
    match.homeGoals !== undefined &&
    match.awayGoals !== null &&
    match.awayGoals !== undefined
  );
}

function TeamRow({ isWinner, name, score }) {
  return (
    <div className={`knockout-team-row${isWinner ? " knockout-team-row--winner" : ""}`}>
      <span>{name || "Equipo pendiente"}</span>
      <strong>{score}</strong>
    </div>
  );
}

function KnockoutMatchCard({
  editable = false,
  isSaving = false,
  match,
  onResultChange,
  onSaveResult,
  resultValue,
}) {
  const hasResult = tieneResultado(match);
  const homeWinner = match.winnerTeamId && match.winnerTeamId === match.homeTeamId;
  const awayWinner = match.winnerTeamId && match.winnerTeamId === match.awayTeamId;
  const tieneGolesCargados =
    resultValue?.homeGoals !== "" &&
    resultValue?.homeGoals !== undefined &&
    resultValue?.awayGoals !== "" &&
    resultValue?.awayGoals !== undefined;

  const resultadoEmpatado =
    tieneGolesCargados &&
    Number(resultValue.homeGoals) ===
    Number(resultValue.awayGoals);

  return (
    <Card className="knockout-match-card">
      <div className="knockout-match-card__meta">
        <Badge size="sm" variant={statusVariant[match.status] || "neutral"}>
          {match.status || "SIN_ESTADO"}
        </Badge>
        <span>#{match.bracketPosition || "-"}</span>
      </div>

      <div className="knockout-match-card__teams">
        <TeamRow
          isWinner={homeWinner}
          name={match.homeTeamName}
          score={hasResult ? match.homeGoals : "-"}
        />
        <TeamRow
          isWinner={awayWinner}
          name={match.awayTeamName}
          score={hasResult ? match.awayGoals : "-"}
        />
      </div>

      <div className="knockout-match-card__footer">
        <span>{formatearFecha(match.startTime)}</span>
        {match.winnerTeamName ? (
          <strong>Ganador: {match.winnerTeamName}</strong>
        ) : (
          <span>Ganador pendiente</span>
        )}
      </div>

      ```jsx
      {editable && (
        <div className="knockout-result-form">
          <div className="admin-field">
            <label htmlFor={`homeGoals-${match.matchId}`}>
              {match.homeTeamName}
            </label>

            <input
              disabled={isSaving}
              id={`homeGoals-${match.matchId}`}
              min={0}
              onChange={(evento) =>
                onResultChange(
                  match.matchId,
                  "homeGoals",
                  evento.target.value
                )
              }
              type="number"
              value={resultValue?.homeGoals ?? ""}
            />
          </div>

          <div className="admin-field">
            <label htmlFor={`awayGoals-${match.matchId}`}>
              {match.awayTeamName}
            </label>

            <input
              disabled={isSaving}
              id={`awayGoals-${match.matchId}`}
              min={0}
              onChange={(evento) =>
                onResultChange(
                  match.matchId,
                  "awayGoals",
                  evento.target.value
                )
              }
              type="number"
              value={resultValue?.awayGoals ?? ""}
            />
          </div>

          {resultadoEmpatado && (
            <>
              <div className="admin-field">
                <label htmlFor={`homePenaltyGoals-${match.matchId}`}>
                  Penales de {match.homeTeamName}
                </label>

                <input
                  disabled={isSaving}
                  id={`homePenaltyGoals-${match.matchId}`}
                  min={0}
                  onChange={(evento) =>
                    onResultChange(
                      match.matchId,
                      "homePenaltyGoals",
                      evento.target.value
                    )
                  }
                  type="number"
                  value={resultValue?.homePenaltyGoals ?? ""}
                />
              </div>

              <div className="admin-field">
                <label htmlFor={`awayPenaltyGoals-${match.matchId}`}>
                  Penales de {match.awayTeamName}
                </label>

                <input
                  disabled={isSaving}
                  id={`awayPenaltyGoals-${match.matchId}`}
                  min={0}
                  onChange={(evento) =>
                    onResultChange(
                      match.matchId,
                      "awayPenaltyGoals",
                      evento.target.value
                    )
                  }
                  type="number"
                  value={resultValue?.awayPenaltyGoals ?? ""}
                />
              </div>
            </>
          )}

          <Button
            isLoading={isSaving}
            onClick={() => onSaveResult(match)}
            type="button"
            variant={hasResult ? "amber" : "primary"}
          >
            {hasResult ? "Corregir resultado" : "Guardar resultado"}
          </Button>
        </div>
      )}
      ```

    </Card>
  );
}

export default KnockoutMatchCard;
