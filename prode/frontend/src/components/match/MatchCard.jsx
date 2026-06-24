import { useState } from "react";
import PredictionForm from "./PredictionForm.jsx";
import Badge from "../ui/Badge.jsx";
import Button from "../ui/Button.jsx";
import Card from "../ui/Card.jsx";

const statusVariant = {
  POR_JUGARSE: "primary",
  EN_JUEGO: "amber",
  FINALIZADO: "success",
};

const statusLabel = {
  POR_JUGARSE: "Por jugarse",
  EN_JUEGO: "En juego",
  FINALIZADO: "Finalizado",
};

function formatearFecha(fecha) {
  if (!fecha) {
    return "Fecha a confirmar";
  }

  const date = new Date(fecha);

  if (Number.isNaN(date.getTime())) {
    return "Fecha a confirmar";
  }

  return date.toLocaleString("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function cargaPronosticoCerrada(fecha) {
  if (!fecha) {
    return false;
  }

  const startTime = new Date(fecha).getTime();

  if (Number.isNaN(startTime)) {
    return false;
  }

  return startTime - Date.now() <= 30 * 60 * 1000;
}

function tieneResultado(match) {
  return (
    match.homeGoals !== null &&
    match.homeGoals !== undefined &&
    match.awayGoals !== null &&
    match.awayGoals !== undefined
  );
}

function MatchCard({
  match,
  onSave,
  onSaved,
  predictionEnabled = true,
  showTournamentBadge = false,
  tournamentName,
}) {
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const status = match.status || "POR_JUGARSE";
  const finalizado = status === "FINALIZADO";
  const puedePronosticar = predictionEnabled && status === "POR_JUGARSE";
  const pronosticoCerrado = puedePronosticar && cargaPronosticoCerrada(match.startTime);
  const resultado = tieneResultado(match)
    ? `${match.homeGoals} - ${match.awayGoals}`
    : "-";
  const nombreTorneo = tournamentName || match.tournamentName;

  function manejarGuardado() {
    setMostrarFormulario(false);

    if (onSaved) {
      onSaved(match);
    }
  }

  return (
    <Card className="match-card">
      <div className="match-card__top">
        <div className="match-card__meta">
          {showTournamentBadge && nombreTorneo && (
            <Badge size="sm" variant="success">
              {nombreTorneo}
            </Badge>
          )}
          <span className="match-card__matchday">
            {match.matchDayName || "Sin fecha asignada"}
          </span>
        </div>
        <Badge size="sm" variant={statusVariant[status] || "neutral"}>
          {statusLabel[status] || status}
        </Badge>
      </div>

      <div className="match-card__teams">
        <strong>{match.homeTeamName || "Equipo local"}</strong>
        <span>VS</span>
        <strong>{match.awayTeamName || "Equipo visitante"}</strong>
      </div>

      <p className="match-card__date">{formatearFecha(match.startTime)}</p>

      <div className={`match-card__result ${finalizado ? "match-card__result--final" : ""}`}>
        <span>{finalizado ? "Resultado final" : "Resultado pendiente"}</span>
        <strong>{finalizado ? resultado : "-"}</strong>
      </div>

      {!predictionEnabled && status === "POR_JUGARSE" && (
        <p className="match-card__closed">
          Este torneo no permite nuevos pronósticos.
        </p>
      )}

      {puedePronosticar && !mostrarFormulario && (
        <>
          <Button
            disabled={pronosticoCerrado}
            fullWidth
            onClick={() => setMostrarFormulario(true)}
            variant="secondary"
          >
            Pronosticar
          </Button>

          {pronosticoCerrado && (
            <p className="match-card__closed">
              La carga de pronósticos está cerrada.
            </p>
          )}
        </>
      )}

      {puedePronosticar && mostrarFormulario && !pronosticoCerrado && (
        <PredictionForm
          match={match}
          onCancel={() => setMostrarFormulario(false)}
          onSave={onSave}
          onSaved={manejarGuardado}
        />
      )}
    </Card>
  );
}

export default MatchCard;
