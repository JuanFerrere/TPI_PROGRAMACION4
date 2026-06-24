import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import {
  obtenerRanking,
  obtenerTorneo,
} from "../services/userTournamentService.js";
import "../App.css";

function iniciales(username) {
  if (!username) {
    return "UT";
  }

  const value = username
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  return value || "UT";
}

function puntosLabel(points) {
  const value = Number(points ?? 0);
  return `${value} ${value === 1 ? "punto" : "puntos"}`;
}

function pronosticosLabel(count) {
  const value = Number(count ?? 0);
  return `${value} ${value === 1 ? "pronóstico" : "pronósticos"}`;
}

function TournamentRankingPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const username = localStorage.getItem("username");
  const [torneo, setTorneo] = useState(null);
  const [ranking, setRanking] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  const lider = ranking[0];
  const posicionActual = useMemo(
    () => ranking.find((item) => item.username === username),
    [ranking, username],
  );
  const podio = ranking.slice(0, 3);

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarDatos() {
      setCargando(true);
      setError("");

      try {
        const [torneoData, rankingData] = await Promise.all([
          obtenerTorneo(tournamentId),
          obtenerRanking(tournamentId),
        ]);
        setTorneo(torneoData);
        setRanking(Array.isArray(rankingData) ? rankingData : []);
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarDatos();
  }, [navigate, tournamentId]);

  return (
    <main className="ranking-page tournament-ranking-page">
      <section className="tournament-ranking-shell">
        <header className="ranking-header tournament-ranking-header">
          <div>
            <Badge variant="success">RANKING</Badge>
            <h1>{torneo?.name || "Ranking del torneo"}</h1>
            <p>
              Clasificación de participantes calculada exclusivamente con los
              pronósticos de esta competencia.
            </p>
          </div>

          <Button
            onClick={() => navigate(`/tournaments/${tournamentId}`)}
            variant="secondary"
          >
            Volver al torneo
          </Button>
        </header>

        {cargando && (
          <div className="predictions-loading tournament-ranking-loading" role="status">
            <Spinner size={28} />
            <span>Cargando ranking...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && ranking.length === 0 && (
          <EmptyState
            action={
              <Button
                onClick={() => navigate(`/tournaments/${tournamentId}`)}
                variant="secondary"
              >
                Volver al torneo
              </Button>
            }
            description="Las posiciones aparecerán cuando los usuarios realicen pronósticos en este torneo."
            title="El ranking todavía está vacío"
          />
        )}

        {!cargando && !error && ranking.length > 0 && (
          <>
            <Card className="tournament-ranking-hero">
              <div>
                <Badge size="sm" variant="success">
                  Clasificación oficial
                </Badge>
                <h2>{torneo?.name || "Torneo"}</h2>
                <p>Participantes ordenados por puntos, aciertos y desempates actuales.</p>
              </div>

              <div className="tournament-ranking-summary">
                <div>
                  <span>Participantes</span>
                  <strong>{ranking.length}</strong>
                </div>
                {lider && (
                  <>
                    <div>
                      <span>Líder actual</span>
                      <strong>{lider.username}</strong>
                    </div>
                    <div>
                      <span>Puntos del líder</span>
                      <strong>{lider.totalPoints}</strong>
                    </div>
                  </>
                )}
                {posicionActual && (
                  <div>
                    <span>Tu posición</span>
                    <strong>#{posicionActual.position}</strong>
                  </div>
                )}
              </div>

              <div className="tournament-ranking-journey" aria-label="Recorrido">
                {["Pronósticos", "Aciertos", "Puntos", "Posición"].map(
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

            <section
              className={`tournament-ranking-podium${
                podio.length === 3 ? " tournament-ranking-podium--complete" : ""
              }`}
              aria-label="Podio del torneo"
            >
              {podio.map((item) => (
                <Card
                  className={`tournament-ranking-podium-card tournament-ranking-podium-card--position-${item.position}`}
                  key={item.userId}
                >
                  <div className="tournament-ranking-podium-card__position">
                    #{item.position}
                  </div>
                  <div className="tournament-ranking-avatar">
                    {iniciales(item.username)}
                  </div>
                  <h2>{item.username}</h2>
                  {item.position === 1 && (
                    <Badge size="sm" variant="amber">
                      Líder
                    </Badge>
                  )}
                  <strong>{puntosLabel(item.totalPoints)}</strong>
                  <span>{item.exactHits} aciertos exactos</span>
                </Card>
              ))}
            </section>

            <Card className="ranking-card tournament-ranking-table-card">
              <div className="tournament-ranking-table-card__header">
                <h2>Clasificación general</h2>
                <p>Todos los participantes ordenados por su rendimiento en el torneo.</p>
              </div>

              <table className="ranking-table tournament-ranking-table">
                <thead>
                  <tr>
                    <th>Posición</th>
                    <th>Usuario</th>
                    <th>Puntos</th>
                    <th>Aciertos exactos</th>
                    <th>Pronósticos</th>
                  </tr>
                </thead>

                <tbody>
                  {ranking.map((item) => {
                    const isCurrentUser = item.username === username;

                    return (
                      <tr
                        className={isCurrentUser ? "ranking-table__me" : undefined}
                        key={item.userId}
                      >
                        <td data-label="Posición">
                          <strong>#{item.position}</strong>
                        </td>
                        <td data-label="Usuario">
                          <div className="tournament-ranking-user">
                            <span className="tournament-ranking-avatar">
                              {iniciales(item.username)}
                            </span>
                            <strong>{item.username}</strong>
                            {isCurrentUser && (
                              <Badge size="sm" variant="success">
                                Vos
                              </Badge>
                            )}
                          </div>
                        </td>
                        <td data-label="Puntos">
                          <strong>{item.totalPoints}</strong>
                        </td>
                        <td data-label="Aciertos exactos">{item.exactHits}</td>
                        <td data-label="Pronósticos">
                          {pronosticosLabel(item.predictionsCount)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </Card>
          </>
        )}
      </section>
    </main>
  );
}

export default TournamentRankingPage;
