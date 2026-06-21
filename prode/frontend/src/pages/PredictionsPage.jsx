import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import PredictionCard from "../components/prediction/PredictionCard.jsx";
import Button from "../components/ui/Button.jsx";
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
        <header className="predictions-header">
          <div>
            <h1>Mis pronósticos</h1>
            <p>Revisá tus resultados, puntos y aciertos.</p>
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

        <div className="predictions-filters" aria-label="Filtros de pronósticos">
          {filtros.map((item) => (
            <Button
              key={item.label}
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
            description="Cuando guardes pronósticos, van a aparecer en esta pantalla."
            title="Todavía no tenés pronósticos"
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
