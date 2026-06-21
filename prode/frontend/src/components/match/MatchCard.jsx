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

function MatchCard({ match }) {
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const status = match.status || "POR_JUGARSE";
  const finalizado = status === "FINALIZADO";
  const puedePronosticar = status === "POR_JUGARSE";
  const pronosticoCerrado = puedePronosticar && cargaPronosticoCerrada(match.startTime);
  const resultado =
    match.homeGoals !== null &&
    match.homeGoals !== undefined &&
    match.awayGoals !== null &&
    match.awayGoals !== undefined
      ? `${match.homeGoals} - ${match.awayGoals}`
      : "-";

  return (
    <Card className="match-card">
      <div className="match-card__top">
        <span>{match.matchDayName || "Fecha sin nombre"}</span>
        <Badge size="sm" variant={statusVariant[status] || "neutral"}>
          {status}
        </Badge>
      </div>

      <div className="match-card__teams">
        <strong>{match.homeTeamName || "Equipo local"}</strong>
        <span>vs</span>
        <strong>{match.awayTeamName || "Equipo visitante"}</strong>
      </div>

      <p className="match-card__date">{formatearFecha(match.startTime)}</p>

      <div className="match-card__result">
        <span>{finalizado ? "Resultado final" : "Resultado pendiente"}</span>
        <strong>{finalizado ? resultado : "-"}</strong>
      </div>

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
              La carga de pronósticos ya está cerrada.
            </p>
          )}
        </>
      )}

      {puedePronosticar && mostrarFormulario && !pronosticoCerrado && (
        <PredictionForm
          match={match}
          onCancel={() => setMostrarFormulario(false)}
        />
      )}
    </Card>
  );
}

export default MatchCard;
