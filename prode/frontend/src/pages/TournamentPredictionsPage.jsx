import { useEffect, useState } from "react";
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

function resultadoReal(prediction) {
  if (prediction.homeGoals === null || prediction.homeGoals === undefined) {
    return "-";
  }

  if (prediction.awayGoals === null || prediction.awayGoals === undefined) {
    return "-";
  }

  return `${prediction.homeGoals} - ${prediction.awayGoals}`;
}

function TournamentPredictionsPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [torneo, setTorneo] = useState(null);
  const [pronosticos, setPronosticos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

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
    <main className="predictions-page">
      <section className="predictions-shell">
        <header className="predictions-header">
          <div>
            <h1>{torneo?.name || "Mis pronosticos"}</h1>
            <p>Revisa tus resultados, puntos y aciertos en este torneo.</p>
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
          <div className="predictions-loading" role="status">
            <Spinner size={28} />
            <span>Cargando pronosticos...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && pronosticos.length === 0 && (
          <EmptyState
            description="Cuando guardes pronosticos en este torneo, van a aparecer aca."
            title="No hay pronosticos"
          />
        )}

        {!cargando && !error && pronosticos.length > 0 && (
          <section className="predictions-grid">
            {pronosticos.map((prediction) => (
              <Card
                className={`prediction-card${
                  prediction.exactHit ? " prediction-card--hit" : ""
                }`}
                key={prediction.id}
              >
                <div className="prediction-card__top">
                  <span>{prediction.matchDayName || "Fecha sin nombre"}</span>
                  <Badge
                    size="sm"
                    variant={statusVariant[prediction.matchStatus] || "neutral"}
                  >
                    {prediction.matchStatus}
                  </Badge>
                </div>

                <div className="prediction-card__match">
                  <strong>{prediction.homeTeamName}</strong>
                  <span>vs</span>
                  <strong>{prediction.awayTeamName}</strong>
                </div>

                <p className="prediction-card__date">
                  {formatearFecha(prediction.matchStartTime)}
                </p>

                <div className="prediction-card__meta">
                  <div>
                    <span>Pronostico</span>
                    <strong>
                      {prediction.predictedHomeGoals} -{" "}
                      {prediction.predictedAwayGoals}
                    </strong>
                  </div>
                  <div>
                    <span>Resultado</span>
                    <strong>{resultadoReal(prediction)}</strong>
                  </div>
                  <div>
                    <span>Tendencia</span>
                    <strong>{prediction.predictedTrend}</strong>
                  </div>
                  <div>
                    <span>Puntos</span>
                    <strong>{prediction.points ?? 0}</strong>
                  </div>
                </div>

                {prediction.exactHit && (
                  <p className="prediction-card__exact-hit">Acierto exacto</p>
                )}
              </Card>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default TournamentPredictionsPage;
