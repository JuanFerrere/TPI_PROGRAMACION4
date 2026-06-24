import Badge from "../ui/Badge.jsx";
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

const trendLabel = {
  LOCAL: "Gana local",
  VISITANTE: "Gana visitante",
  EMPATE: "Empate",
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

function tieneResultado(prediction) {
  return (
    prediction.homeGoals !== null &&
    prediction.homeGoals !== undefined &&
    prediction.awayGoals !== null &&
    prediction.awayGoals !== undefined
  );
}

function obtenerResultadoFinal(prediction) {
  if (!tieneResultado(prediction)) {
    return "Resultado no disponible";
  }

  return `${prediction.homeGoals} - ${prediction.awayGoals}`;
}

function obtenerTipoAcierto(prediction) {
  if (prediction.matchStatus !== "FINALIZADO") {
    return {
      className: "prediction-card--pending",
      label: "Resultado pendiente",
      variant: "primary",
    };
  }

  if (prediction.exactHit) {
    return {
      className: "prediction-card--exact",
      label: "Acierto exacto",
      variant: "amber",
    };
  }

  if (Number(prediction.points ?? 0) > 0) {
    return {
      className: "prediction-card--trend",
      label: "Tendencia correcta",
      variant: "success",
    };
  }

  return {
    className: "prediction-card--zero",
    label: "Sin puntos",
    variant: "neutral",
  };
}

function PredictionCard({ prediction }) {
  const finalizado = prediction.matchStatus === "FINALIZADO";
  const tipoAcierto = obtenerTipoAcierto(prediction);
  const estadoTraducido =
    statusLabel[prediction.matchStatus] || prediction.matchStatus || "Sin estado";
  const tendenciaTraducida =
    trendLabel[prediction.predictedTrend] || "Sin tendencia";
  const marcadorPronosticado = `${prediction.predictedHomeGoals} - ${prediction.predictedAwayGoals}`;

  return (
    <Card className={`prediction-card ${tipoAcierto.className}`}>
      <div className="prediction-card__top">
        <div className="prediction-card__eyebrow">
          {prediction.tournamentName && (
            <Badge size="sm" variant="neutral">
              {prediction.tournamentName}
            </Badge>
          )}
          <span>{prediction.matchDayName || "Fecha sin nombre"}</span>
        </div>
        <Badge size="sm" variant={statusVariant[prediction.matchStatus] || "neutral"}>
          {estadoTraducido}
        </Badge>
      </div>

      <p className="prediction-card__date">
        {formatearFecha(prediction.matchStartTime)}
      </p>

      <div className="prediction-card__match">
        <strong>{prediction.homeTeamName || "Equipo local"}</strong>
        <div className="prediction-card__score">
          <span>Tu pronóstico</span>
          <strong>{marcadorPronosticado}</strong>
        </div>
        <strong>{prediction.awayTeamName || "Equipo visitante"}</strong>
      </div>

      {!finalizado && (
        <div className="prediction-card__pending">
          <div>
            <span>Estado</span>
            <strong>
              {prediction.matchStatus === "EN_JUEGO" ? "Partido en juego" : "Pendiente"}
            </strong>
          </div>
          <p>El resultado todavía no fue cargado.</p>
          <Badge size="sm" variant="primary">
            {tendenciaTraducida}
          </Badge>
        </div>
      )}

      {finalizado && (
        <>
          <div className="prediction-card__details">
            <div>
              <span>Tu pronóstico</span>
              <strong>{marcadorPronosticado}</strong>
            </div>
            <div>
              <span>Resultado final</span>
              <strong>{obtenerResultadoFinal(prediction)}</strong>
            </div>
            <div>
              <span>Puntos obtenidos</span>
              <strong>{prediction.points ?? 0}</strong>
            </div>
          </div>

          <div className="prediction-card__outcome">
            <Badge size="sm" variant={tipoAcierto.variant}>
              {tipoAcierto.label}
            </Badge>
            <span>{tendenciaTraducida}</span>
          </div>
        </>
      )}
    </Card>
  );
}

export default PredictionCard;
