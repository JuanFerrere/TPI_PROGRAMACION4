import { useEffect, useState } from "react";
import { getGlobalRanking } from "../services/rankingService";
import "../App.css";

function RankingPage() {
  const [ranking, setRanking] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    cargarRanking();
  }, []);

  async function cargarRanking() {
    try {
      setCargando(true);
      setError("");

      const data = await getGlobalRanking();
      setRanking(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="ranking-page">
      <section className="ranking-header">
        <p className="eyebrow">Prode UTN</p>
        <h1>Ranking global</h1>
        <p>
          Consultá la tabla de posiciones general según los puntos obtenidos por
          cada usuario.
        </p>
      </section>

      {cargando && <p className="ranking-message">Cargando ranking...</p>}

      {error && <p className="ranking-error">{error}</p>}

      {!cargando && !error && ranking.length === 0 && (
        <p className="ranking-message">
          Todavía no hay datos para mostrar en el ranking.
        </p>
      )}

      {!cargando && !error && ranking.length > 0 && (
        <section className="ranking-card">
          <table className="ranking-table">
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
              {ranking.map((item) => (
                <tr key={item.userId}>
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

export default RankingPage;