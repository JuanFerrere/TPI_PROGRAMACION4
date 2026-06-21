import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { obtenerTorneo } from "../services/userTournamentService.js";
import "../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const estadoVariant = {
  ACTIVE: "success",
  FINISHED: "primary",
};

function TournamentHomePage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [torneo, setTorneo] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarTorneo() {
      setCargando(true);
      setError("");

      try {
        setTorneo(await obtenerTorneo(tournamentId));
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarTorneo();
  }, [navigate, tournamentId]);

  return (
    <main className="tournament-page">
      <section className="tournament-shell">
        <header className="tournament-header">
          <div>
            <Badge variant="primary">Torneo</Badge>
            <h1>{torneo?.name || "Torneo"}</h1>
            <p>{torneo?.description || "Pronostica partidos y subi en el ranking."}</p>
          </div>

          <Button onClick={() => navigate("/tournaments")} variant="secondary">
            Volver a torneos
          </Button>
        </header>

        {cargando && (
          <div className="matches-loading" role="status">
            <Spinner size={28} />
            <span>Cargando torneo...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && torneo && (
          <>
            <Card className="tournament-summary-card">
              <div className="tournament-card__badges">
                <Badge size="sm" variant={estadoVariant[torneo.status] || "neutral"}>
                  {torneo.status}
                </Badge>
                <Badge size="sm" variant="amber">
                  {formatoLabel[torneo.format || "LEAGUE"]}
                </Badge>
              </div>
              <h2>{torneo.name}</h2>
              <p>
                {torneo.status === "FINISHED"
                  ? "El torneo finalizo. Podes revisar partidos, pronosticos y ranking."
                  : "El torneo esta activo. Ya podes cargar y actualizar tus pronosticos."}
              </p>
            </Card>

            <section className="tournament-grid">
              <Card className="tournament-card">
                <Badge size="sm" variant="primary">Fixture</Badge>
                <h2>Partidos y pronosticos</h2>
                <p>Elegí los partidos del torneo y guarda tus marcadores.</p>
                <Button
                  fullWidth
                  onClick={() => navigate(`/tournaments/${tournamentId}/matches`)}
                  variant="secondary"
                >
                  Ver partidos
                </Button>
              </Card>

              <Card className="tournament-card">
                <Badge size="sm" variant="amber">Mis jugadas</Badge>
                <h2>Mis pronosticos</h2>
                <p>Consulta tus pronosticos, resultados y puntos del torneo.</p>
                <Button
                  fullWidth
                  onClick={() => navigate(`/tournaments/${tournamentId}/predictions`)}
                  variant="secondary"
                >
                  Ver pronosticos
                </Button>
              </Card>

              <Card className="tournament-card">
                <Badge size="sm" variant="success">Ranking</Badge>
                <h2>Ranking</h2>
                <p>Compará tus puntos contra otros usuarios en este torneo.</p>
                <Button
                  fullWidth
                  onClick={() => navigate(`/tournaments/${tournamentId}/ranking`)}
                  variant="secondary"
                >
                  Ver ranking
                </Button>
              </Card>
            </section>
          </>
        )}
      </section>
    </main>
  );
}

export default TournamentHomePage;
