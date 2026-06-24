import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import {
  obtenerMisPronosticos,
  obtenerTorneo,
} from "../services/userTournamentService.js";
import "../App.css";

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

function resultadoReal(prediction) {
  if (!tieneResultado(prediction)) {
    return "Pendiente";
  }

  return `${prediction.homeGoals} - ${prediction.awayGoals}`;
}

function puntosLabel(prediction) {
  if (prediction.matchStatus !== "FINALIZADO") {
    return "Pendientes";
  }

  const puntos = Number(prediction.points ?? 0);
  return `${puntos} ${puntos === 1 ? "punto" : "puntos"}`;
}

function obtenerClasificacion(prediction) {
  if (prediction.matchStatus !== "FINALIZADO") {
    return {
      className: "tournament-prediction-card--pending",
      label: "Resultado pendiente",
      variant: "primary",
    };
  }

  if (prediction.exactHit) {
    return {
      className: "tournament-prediction-card--exact",
      label: "Acierto exacto",
      variant: "amber",
    };
  }

  if (Number(prediction.points ?? 0) > 0) {
    return {
      className: "tournament-prediction-card--trend",
      label: "Tendencia correcta",
      variant: "success",
    };
  }

  return {
    className: "tournament-prediction-card--zero",
    label: "Sin puntos",
    variant: "neutral",
  };
}

function TournamentPredictionsPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [torneo, setTorneo] = useState(null);
  const [pronosticos, setPronosticos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  const resumen = useMemo(() => {
    const total = pronosticos.length;
    const finalizados = pronosticos.filter(
      (prediction) => prediction.matchStatus === "FINALIZADO",
    ).length;
    const pendientes = pronosticos.filter(
      (prediction) => prediction.matchStatus !== "FINALIZADO",
    ).length;
    const puntos = pronosticos.reduce(
      (totalPuntos, prediction) => totalPuntos + Number(prediction.points ?? 0),
      0,
    );
    const puedeCalcularExactos = pronosticos.some((prediction) =>
      Object.prototype.hasOwnProperty.call(prediction, "exactHit"),
    );
    const exactos = pronosticos.filter((prediction) => prediction.exactHit).length;

    return { exactos, finalizados, pendientes, puedeCalcularExactos, puntos, total };
  }, [pronosticos]);

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarDatos() {
      setCargando(true);
      setError("");

      try {
        const [torneoData, pronosticosData] = await Promise.all([
          obtenerTorneo(tournamentId),
          obtenerMisPronosticos(tournamentId),
        ]);
        setTorneo(torneoData);
        setPronosticos(Array.isArray(pronosticosData) ? pronosticosData : []);
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarDatos();
  }, [navigate, tournamentId]);

  return (
    <main className="predictions-page tournament-predictions-page">
      <section className="predictions-shell tournament-predictions-shell">
        <header className="predictions-header tournament-predictions-header">
          <div>
            <Badge variant="primary">MIS PRONÓSTICOS</Badge>
            <h1>{torneo?.name || "Mis pronósticos"}</h1>
            <p>
              Revisá tus pronósticos, resultados y puntos obtenidos en esta
              competencia.
            </p>
          </div>

          <div className="predictions-header__actions">
            <Button
              onClick={() => navigate(`/tournaments/${tournamentId}/matches`)}
              variant="secondary"
            >
              Ver partidos
            </Button>
            <Button
              onClick={() => navigate(`/tournaments/${tournamentId}`)}
              variant="secondary"
            >
              Volver al torneo
            </Button>
          </div>
        </header>

        {cargando && (
          <div className="predictions-loading tournament-predictions-loading" role="status">
            <Spinner size={28} />
            <span>Cargando pronósticos...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && pronosticos.length === 0 && (
          <EmptyState
            action={
              <Button
                onClick={() => navigate(`/tournaments/${tournamentId}/matches`)}
              >
                Ver partidos
              </Button>
            }
            description="Consultá los partidos del torneo y cargá tu primera jugada."
            title="Todavía no hiciste pronósticos"
          />
        )}

        {!cargando && !error && pronosticos.length > 0 && (
          <>
            <Card className="tournament-predictions-hero">
              <div>
                <Badge size="sm" variant="amber">
                  Historial del torneo
                </Badge>
                <h2>{torneo?.name || "Torneo"}</h2>
                <p>Tu seguimiento personal de jugadas, resultados y puntos.</p>
              </div>

              <div className="tournament-predictions-summary">
                <div>
                  <span>Total de pronósticos</span>
                  <strong>{resumen.total}</strong>
                </div>
                <div>
                  <span>Pendientes</span>
                  <strong>{resumen.pendientes}</strong>
                </div>
                <div>
                  <span>Finalizados</span>
                  <strong>{resumen.finalizados}</strong>
                </div>
                <div>
                  <span>Puntos obtenidos</span>
                  <strong>{resumen.puntos}</strong>
                </div>
                {resumen.puedeCalcularExactos && (
                  <div>
                    <span>Aciertos exactos</span>
                    <strong>{resumen.exactos}</strong>
                  </div>
                )}
              </div>

              <div className="tournament-predictions-journey" aria-label="Recorrido">
                {["Pronosticar", "Esperar resultado", "Obtener puntos", "Revisar posición"].map(
                  (paso, index, pasos) => (
                    <span key={paso}>
                      {paso}
                      {index < pasos.length - 1 && (
                        <strong aria-hidden="true">→</strong>
                      )}
                    </span>
                  ),
                )}
              </div>
            </Card>

            <section className="tournament-predictions-grid">
              {pronosticos.map((prediction) => {
                const clasificacion = obtenerClasificacion(prediction);
                const finalizado = prediction.matchStatus === "FINALIZADO";
                const marcadorPronosticado = `${prediction.predictedHomeGoals} - ${prediction.predictedAwayGoals}`;
                const tendencia =
                  trendLabel[prediction.predictedTrend] || "Sin tendencia";

                return (
                  <Card
                    className={`tournament-prediction-card ${clasificacion.className}`}
                    key={prediction.id}
                  >
                    <div className="tournament-prediction-card__top">
                      <div>
                        <span>{prediction.matchDayName || "Fecha sin nombre"}</span>
                        <strong>{formatearFecha(prediction.matchStartTime)}</strong>
                      </div>
                      <Badge
                        size="sm"
                        variant={statusVariant[prediction.matchStatus] || "neutral"}
                      >
                        {statusLabel[prediction.matchStatus] ||
                          prediction.matchStatus ||
                          "Sin estado"}
                      </Badge>
                    </div>

                    <div className="tournament-prediction-card__match">
                      <strong>{prediction.homeTeamName || "Equipo local"}</strong>
                      <span>{finalizado && tieneResultado(prediction) ? resultadoReal(prediction) : "VS"}</span>
                      <strong>{prediction.awayTeamName || "Equipo visitante"}</strong>
                    </div>

                    <div className="tournament-prediction-card__scoreboard">
                      <div className="tournament-prediction-card__score-block tournament-prediction-card__score-block--prediction">
                        <span>Tu pronóstico</span>
                        <strong>{marcadorPronosticado}</strong>
                      </div>
                      <div className="tournament-prediction-card__score-block tournament-prediction-card__score-block--result">
                        <span>{finalizado ? "Resultado final" : "Resultado"}</span>
                        <strong>{resultadoReal(prediction)}</strong>
                      </div>
                    </div>

                    <div className="tournament-prediction-card__details">
                      <div>
                        <span>Tendencia pronosticada</span>
                        <strong>{tendencia}</strong>
                      </div>
                      <div>
                        <span>Puntos obtenidos</span>
                        <strong>{puntosLabel(prediction)}</strong>
                      </div>
                    </div>

                    <div className="tournament-prediction-card__footer">
                      <Badge size="sm" variant={clasificacion.variant}>
                        {clasificacion.label}
                      </Badge>
                    </div>
                  </Card>
                );
              })}
            </section>
          </>
        )}
      </section>
    </main>
  );
}

export default TournamentPredictionsPage;
