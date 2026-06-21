import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { obtenerTorneosDisponibles } from "../services/userTournamentService.js";
import "../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const estadoVariant = {
  ACTIVE: "success",
  FINISHED: "primary",
};

function TournamentsPage() {
  const navigate = useNavigate();
  const [torneos, setTorneos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarTorneos() {
      setCargando(true);
      setError("");

      try {
        const data = await obtenerTorneosDisponibles();
        setTorneos(Array.isArray(data) ? data : []);
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarTorneos();
  }, [navigate]);

  return (
    <main className="tournament-page">
      <section className="tournament-shell">
        <header className="tournament-header">
          <div>
            <Badge variant="primary">Competencias</Badge>
            <h1>Torneos disponibles</h1>
            <p>Elegi una competencia para pronosticar y competir en su ranking.</p>
          </div>

          <Button onClick={() => navigate("/dashboard")} variant="secondary">
            Volver al dashboard
          </Button>
        </header>

        {cargando && (
          <div className="matches-loading" role="status">
            <Spinner size={28} />
            <span>Cargando torneos...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && torneos.length === 0 && (
          <EmptyState
            description="Cuando haya torneos ACTIVE o FINISHED, van a aparecer aca."
            title="No hay torneos disponibles"
          />
        )}

        {!cargando && !error && torneos.length > 0 && (
          <section className="tournament-grid">
            {torneos.map((torneo) => (
              <Card className="tournament-card" key={torneo.id}>
                <div className="tournament-card__badges">
                  <Badge
                    size="sm"
                    variant={estadoVariant[torneo.status] || "neutral"}
                  >
                    {torneo.status}
                  </Badge>
                  <Badge size="sm" variant="amber">
                    {formatoLabel[torneo.format || "LEAGUE"]}
                  </Badge>
                </div>
                <h2>{torneo.name}</h2>
                <p>{torneo.description || "Torneo sin descripcion."}</p>
                <Button
                  fullWidth
                  onClick={() => navigate(`/tournaments/${torneo.id}`)}
                  variant="secondary"
                >
                  Entrar al torneo
                </Button>
              </Card>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default TournamentsPage;
