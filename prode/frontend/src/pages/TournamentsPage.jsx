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

const formatoVariant = {
  GROUPS: "primary",
  LEAGUE: "amber",
};

const estadoLabel = {
  ACTIVE: "Activo",
  FINISHED: "Finalizado",
};

const estadoVariant = {
  ACTIVE: "success",
  FINISHED: "primary",
};

function obtenerIniciales(nombre) {
  if (!nombre) {
    return "PR";
  }

  const iniciales = nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((palabra) => palabra[0])
    .join("")
    .toUpperCase();

  return iniciales || "PR";
}

function TournamentSelectionCard({ torneo }) {
  const navigate = useNavigate();
  const activo = torneo.status === "ACTIVE";
  const formato = torneo.format || "LEAGUE";
  const recorrido = activo
    ? ["Partidos", "Pronósticos", "Resultados"]
    : ["Partidos", "Resultados", "Historial"];

  return (
    <Card
      className={`tournament-selection-card tournament-selection-card--${
        activo ? "active" : "finished"
      }`}
    >
      <div className="tournament-selection-card__top">
        <div className="tournament-selection-card__badges">
          <Badge size="sm" variant={estadoVariant[torneo.status] || "neutral"}>
            {estadoLabel[torneo.status] || torneo.status || "Sin estado"}
          </Badge>
          <Badge size="sm" variant={formatoVariant[formato] || "neutral"}>
            {formatoLabel[formato] || "Tabla general"}
          </Badge>
        </div>
        <span className="tournament-selection-card__identifier" aria-hidden="true">
          {obtenerIniciales(torneo.name)}
        </span>
      </div>

      <div className="tournament-selection-card__body">
        <h3>{torneo.name}</h3>
        <p>{torneo.description || "Competencia sin descripción."}</p>
      </div>

      <div className="tournament-selection-card__footer">
        <div
          className="tournament-selection-card__journey"
          aria-label="Recorrido del torneo"
        >
          {recorrido.map((paso, index) => (
            <span key={paso}>
              {paso}
              {index < recorrido.length - 1 && (
                <strong aria-hidden="true">→</strong>
              )}
            </span>
          ))}
        </div>
        <Button
          fullWidth
          onClick={() => navigate(`/tournaments/${torneo.id}`)}
          variant={activo ? "success" : "secondary"}
        >
          {activo ? "Entrar al torneo" : "Consultar torneo"}
        </Button>
      </div>
    </Card>
  );
}

function TournamentSection({ description, title, torneos }) {
  if (torneos.length === 0) {
    return null;
  }

  return (
    <section className="tournament-catalog-section">
      <div className="tournament-catalog-section__header">
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
      <div className="tournament-catalog-grid">
        {torneos.map((torneo) => (
          <TournamentSelectionCard key={torneo.id} torneo={torneo} />
        ))}
      </div>
    </section>
  );
}

function TournamentsPage() {
  const navigate = useNavigate();
  const [torneos, setTorneos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  const torneosActivos = torneos.filter((torneo) => torneo.status === "ACTIVE");
  const torneosFinalizados = torneos.filter(
    (torneo) => torneo.status === "FINISHED",
  );

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
    <main className="tournament-page tournaments-catalog-page">
      <section className="tournament-shell tournaments-catalog-shell">
        <header className="tournament-header tournament-catalog-header">
          <div>
            <Badge variant="primary">COMPETENCIAS</Badge>
            <h1>Torneos disponibles</h1>
            <p>
              Elegí una competencia para consultar sus partidos, realizar
              pronósticos y seguir tus jugadas.
            </p>
          </div>

          <Button onClick={() => navigate("/dashboard")} variant="secondary">
            Volver al dashboard
          </Button>
        </header>

        {cargando && (
          <div className="matches-loading tournaments-catalog-loading" role="status">
            <Spinner size={28} />
            <span>Cargando torneos...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && torneos.length === 0 && (
          <EmptyState
            action={
              <Button onClick={() => navigate("/dashboard")} variant="secondary">
                Volver al dashboard
              </Button>
            }
            description="Todavía no existen competencias habilitadas para consultar."
            title="No hay torneos disponibles"
          />
        )}

        {!cargando && !error && torneos.length > 0 && (
          <div className="tournament-catalog-content">
            <TournamentSection
              description="Competencias disponibles para consultar partidos y realizar pronósticos."
              title="Torneos activos"
              torneos={torneosActivos}
            />
            <TournamentSection
              description="Competencias cerradas que todavía podés consultar."
              title="Torneos finalizados"
              torneos={torneosFinalizados}
            />
          </div>
        )}
      </section>
    </main>
  );
}

export default TournamentsPage;
