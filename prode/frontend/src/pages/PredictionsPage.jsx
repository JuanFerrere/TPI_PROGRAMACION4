import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import PredictionCard from "../components/prediction/PredictionCard.jsx";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { obtenerMisPronosticos } from "../services/predictionService.js";
import "../App.css";

const filtros = [
  { label: "Todos", value: "" },
  { label: "Por jugarse", value: "POR_JUGARSE" },
  { label: "En juego", value: "EN_JUEGO" },
  { label: "Finalizados", value: "FINALIZADO" },
];

function PredictionsPage() {
  const navigate = useNavigate();
  const [filtro, setFiltro] = useState("");
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
    let activo = true;

    async function cargarPronosticos() {
      if (!localStorage.getItem("token")) {
        navigate("/login", { replace: true });
        return;
      }

      setCargando(true);
      setError("");

      try {
        const data = await obtenerMisPronosticos(filtro || undefined);

        if (activo) {
          setPronosticos(Array.isArray(data) ? data : []);
        }
      } catch (error) {
        if (activo) {
          setError(error.message);
        }
      } finally {
        if (activo) {
          setCargando(false);
        }
      }
    }

    cargarPronosticos();

    return () => {
      activo = false;
    };
  }, [filtro, navigate]);

  return (
    <main className="predictions-page">
      <section className="predictions-shell">
        <header className="predictions-header predictions-header--premium">
          <div>
            <Badge variant="primary">MIS JUGADAS</Badge>
            <h1>Mis pronósticos</h1>
            <p>Revisá tus jugadas, resultados y puntos obtenidos.</p>
          </div>

          <div className="predictions-header__actions">
            <Button onClick={() => navigate("/matches")} variant="secondary">
              Ver partidos
            </Button>
            <Button onClick={() => navigate("/dashboard")} variant="secondary">
              Volver al dashboard
            </Button>
          </div>
        </header>

        {!cargando && !error && pronosticos.length > 0 && (
          <section className="predictions-summary" aria-label="Resumen de pronósticos">
            <Card className="predictions-summary-card">
              <span>Total de pronósticos</span>
              <strong>{resumen.total}</strong>
            </Card>
            <Card className="predictions-summary-card predictions-summary-card--pending">
              <span>Pendientes</span>
              <strong>{resumen.pendientes}</strong>
            </Card>
            <Card className="predictions-summary-card predictions-summary-card--finished">
              <span>Finalizados</span>
              <strong>{resumen.finalizados}</strong>
            </Card>
            <Card className="predictions-summary-card predictions-summary-card--points">
              <span>Puntos obtenidos</span>
              <strong>{resumen.puntos}</strong>
            </Card>
            {resumen.puedeCalcularExactos && (
              <Card className="predictions-summary-card predictions-summary-card--exact">
                <span>Aciertos exactos</span>
                <strong>{resumen.exactos}</strong>
              </Card>
            )}
          </section>
        )}

        <div
          className="predictions-filters predictions-filters--segmented"
          aria-label="Filtros de pronósticos"
        >
          {filtros.map((item) => (
            <Button
              key={item.label}
              aria-pressed={filtro === item.value}
              className="predictions-filter-button"
              onClick={() => setFiltro(item.value)}
              variant={filtro === item.value ? "primary" : "secondary"}
            >
              {item.label}
            </Button>
          ))}
        </div>

        {cargando && (
          <div className="predictions-loading" role="status">
            <Spinner size={28} />
            <span>Cargando pronósticos...</span>
          </div>
        )}

        {!cargando && error && <ErrorMessage message={error} />}

        {!cargando && !error && pronosticos.length === 0 && (
          <EmptyState
            action={
              !filtro ? (
                <Button onClick={() => navigate("/matches")}>Ver partidos</Button>
              ) : null
            }
            description={
              filtro
                ? "Probá seleccionando otro estado."
                : "Entrá a los partidos disponibles y cargá tu primera jugada."
            }
            title={
              filtro
                ? "No hay pronósticos en esta categoría"
                : "Todavía no hiciste pronósticos"
            }
          />
        )}

        {!cargando && !error && pronosticos.length > 0 && (
          <section
            aria-label="Listado de mis pronósticos"
            className="predictions-grid"
          >
            {pronosticos.map((prediction) => (
              <PredictionCard key={prediction.id} prediction={prediction} />
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default PredictionsPage;
