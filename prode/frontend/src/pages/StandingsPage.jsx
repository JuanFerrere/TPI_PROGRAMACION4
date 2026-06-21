import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { getStandings } from "../services/standingService.js";
import "../App.css";

function obtenerTituloGrupo(groupName, index) {
  if (!groupName) {
    return "Tabla general";
  }

  const nombre = String(groupName).trim();

  if (!nombre) {
    return `Grupo ${index + 1}`;
  }

  if (nombre.toLowerCase().startsWith("grupo ")) {
    return nombre;
  }

  return `Grupo ${nombre}`;
}

function formatearDiferenciaGol(goalDifference) {
  if (typeof goalDifference !== "number") {
    return goalDifference;
  }

  return goalDifference > 0 ? `+${goalDifference}` : goalDifference;
}

function StandingsPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [standings, setStandings] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarTabla() {
      setCargando(true);
      setError("");

      try {
        setStandings(await getStandings(tournamentId));
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarTabla();
  }, [navigate, tournamentId]);

  const groups = Array.isArray(standings?.groups) ? standings.groups : [];
  const tieneFilas = groups.some(
    (group) => Array.isArray(group.rows) && group.rows.length > 0
  );

  return (
    <main className="ranking-page tournament-ranking-page standings-page">
      <section className="ranking-header">
        <Badge variant="primary">Tabla deportiva</Badge>
        <h1>{standings?.tournamentName || "Tabla deportiva"}</h1>
        <p>Posiciones, puntos, goles y diferencia de gol del torneo.</p>

        <div className="standings-header-actions">
          {standings?.format && (
            <Badge size="sm" variant="amber">
              {standings.format}
            </Badge>
          )}
          <Button
            onClick={() => navigate(`/tournaments/${tournamentId}`)}
            variant="secondary"
          >
            Volver al torneo
          </Button>
        </div>
      </section>

      {cargando && (
        <div className="predictions-loading" role="status">
          <Spinner size={28} />
          <span>Cargando tabla deportiva...</span>
        </div>
      )}

      {!cargando && <ErrorMessage message={error} />}

      {!cargando && !error && !tieneFilas && (
        <EmptyState
          description="Cuando el torneo tenga equipos cargados, la tabla aparecera aca."
          title="Tabla deportiva vacia"
        />
      )}

      {!cargando && !error && tieneFilas && (
        <section className="standings-groups">
          {groups.map((group, index) => {
            const rows = Array.isArray(group.rows) ? group.rows : [];

            if (rows.length === 0) {
              return null;
            }

            return (
              <Card
                className="standings-group-card"
                key={group.groupName || `standings-group-${index}`}
                padding="none"
              >
                <div className="standings-group-card__header">
                  <div>
                    <Badge size="sm" variant="success">
                      {obtenerTituloGrupo(group.groupName, index)}
                    </Badge>
                    <h2>{obtenerTituloGrupo(group.groupName, index)}</h2>
                  </div>
                  <span>{rows.length} equipos</span>
                </div>

                <div className="standings-table-scroll">
                  <table className="ranking-table standings-table">
                    <thead>
                      <tr>
                        <th>Pos</th>
                        <th>Equipo</th>
                        <th>PJ</th>
                        <th>G</th>
                        <th>E</th>
                        <th>P</th>
                        <th>GF</th>
                        <th>GC</th>
                        <th>DG</th>
                        <th>Pts</th>
                      </tr>
                    </thead>

                    <tbody>
                      {rows.map((row) => (
                        <tr key={row.teamId}>
                          <td>#{row.position}</td>
                          <td>{row.teamName}</td>
                          <td>{row.played}</td>
                          <td>{row.won}</td>
                          <td>{row.drawn}</td>
                          <td>{row.lost}</td>
                          <td>{row.goalsFor}</td>
                          <td>{row.goalsAgainst}</td>
                          <td>{formatearDiferenciaGol(row.goalDifference)}</td>
                          <td>
                            <strong>{row.points}</strong>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            );
          })}
        </section>
      )}
    </main>
  );
}

export default StandingsPage;
