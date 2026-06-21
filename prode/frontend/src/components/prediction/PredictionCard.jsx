import Badge from "../ui/Badge.jsx";
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

function obtenerResumen(prediction) {
  if (prediction.matchStatus === "POR_JUGARSE") {
    return "Pendiente de resultado";
  }

  if (prediction.matchStatus === "EN_JUEGO") {
    return "Partido en juego";
  }

  return `${prediction.points ?? 0} puntos`;
}

function PredictionCard({ prediction }) {
  const finalizado = prediction.matchStatus === "FINALIZADO";
  const exactHit = finalizado && prediction.exactHit;

  return (
    <Card className={`prediction-card${exactHit ? " prediction-card--hit" : ""}`}>
      <div className="prediction-card__top">
        <span>{prediction.matchDayName || "Fecha sin nombre"}</span>
        <Badge size="sm" variant={statusVariant[prediction.matchStatus] || "neutral"}>
          {prediction.matchStatus || "SIN_ESTADO"}
        </Badge>
      </div>

      <div className="prediction-card__match">
        <strong>{prediction.homeTeamName || "Equipo local"}</strong>
        <span>vs</span>
        <strong>{prediction.awayTeamName || "Equipo visitante"}</strong>
      </div>

      <p className="prediction-card__date">
        {formatearFecha(prediction.matchStartTime)}
      </p>

      <div className="prediction-card__score">
        <span>Pronóstico</span>
        <strong>
          {prediction.predictedHomeGoals} - {prediction.predictedAwayGoals}
        </strong>
      </div>

      <div className="prediction-card__meta">
        <div>
          <span>Tendencia</span>
          <strong>{prediction.predictedTrend || "Sin tendencia"}</strong>
        </div>
        <div>
          <span>Resultado</span>
          <strong>{obtenerResumen(prediction)}</strong>
        </div>
      </div>

      {exactHit && (
        <p className="prediction-card__exact-hit">Acierto exacto</p>
      )}
    </Card>
  );
}

export default PredictionCard;
