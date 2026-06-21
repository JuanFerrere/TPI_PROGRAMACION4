import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import {
  obtenerRanking,
  obtenerTorneo,
} from "../services/userTournamentService.js";
import "../App.css";

function TournamentRankingPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const username = localStorage.getItem("username");
  const [torneo, setTorneo] = useState(null);
  const [ranking, setRanking] = useState([]);
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
      <section className="ranking-header">
        <Badge variant="success">Ranking</Badge>
        <h1>{torneo?.name || "Ranking del torneo"}</h1>
        <p>Tabla de usuarios calculada solo con pronosticos de este torneo.</p>
        <Button
          onClick={() => navigate(`/tournaments/${tournamentId}`)}
          variant="secondary"
        >
          Volver al torneo
        </Button>
      </section>

      {cargando && (
        <div className="predictions-loading" role="status">
          <Spinner size={28} />
          <span>Cargando ranking...</span>
        </div>
      )}

      {!cargando && <ErrorMessage message={error} />}

      {!cargando && !error && ranking.length === 0 && (
        <EmptyState
          description="Todavia no hay pronosticos con puntos para este torneo."
          title="Ranking vacio"
        />
      )}

      {!cargando && !error && ranking.length > 0 && (
        <section className="ranking-card">
          <table className="ranking-table">
            <thead>
              <tr>
                <th>Posicion</th>
                <th>Usuario</th>
                <th>Puntos</th>
                <th>Aciertos exactos</th>
                <th>Pronosticos</th>
              </tr>
            </thead>

            <tbody>
              {ranking.map((item) => (
                <tr
                  className={
                    item.username === username ? "ranking-table__me" : undefined
                  }
                  key={item.userId}
                >
                  <td>#{item.position}</td>
                  <td>{item.username}</td>
                  <td>{item.totalPoints}</td>
                  <td>{item.exactHits}</td>
                  <td>{item.predictionsCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </main>
  );
}

export default TournamentRankingPage;
