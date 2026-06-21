import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import MatchCard from "../components/match/MatchCard.jsx";
import Button from "../components/ui/Button.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { obtenerPartidos } from "../services/matchService.js";
import "../App.css";

function MatchesPage() {
  const navigate = useNavigate();
  const [partidos, setPartidos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let activo = true;

    async function cargarPartidos() {
      if (!localStorage.getItem("token")) {
        navigate("/login", { replace: true });
        return;
      }

      setCargando(true);
      setError("");

      try {
        const data = await obtenerPartidos();

        if (activo) {
          setPartidos(Array.isArray(data) ? data : []);
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

    cargarPartidos();

    return () => {
      activo = false;
    };
  }, [navigate]);

  return (
    <main className="matches-page">
      <section className="matches-shell">
        <header className="matches-header">
          <div>
            <h1>Partidos</h1>
            <p>Elegí un partido y prepará tu pronóstico.</p>
          </div>

          <div className="matches-header__actions">
            <Button onClick={() => navigate("/predictions")} variant="secondary">
              Mis pronósticos
            </Button>
            <Button onClick={() => navigate("/dashboard")} variant="secondary">
              Volver al dashboard
            </Button>
          </div>
        </header>

        {cargando && (
          <div className="matches-loading" role="status">
            <Spinner size={28} />
            <span>Cargando partidos...</span>
          </div>
        )}

        {!cargando && error && <ErrorMessage message={error} />}

        {!cargando && !error && partidos.length === 0 && (
          <EmptyState
            description="Cuando haya partidos cargados, van a aparecer en esta pantalla."
            title="No hay partidos disponibles"
          />
        )}

        {!cargando && !error && partidos.length > 0 && (
          <section className="matches-grid" aria-label="Listado de partidos">
            {partidos.map((partido) => (
              <MatchCard key={partido.id} match={partido} />
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default MatchesPage;
