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

function MatchCard({ match }) {
  const status = match.status || "POR_JUGARSE";
  const finalizado = status === "FINALIZADO";
  const puedePronosticar = status === "POR_JUGARSE";
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

      {puedePronosticar && (
        <Button disabled fullWidth variant="secondary">
          Pronosticar
        </Button>
      )}
    </Card>
  );
}

export default MatchCard;
