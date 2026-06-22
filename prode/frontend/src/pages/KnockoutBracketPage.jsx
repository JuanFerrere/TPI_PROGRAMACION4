import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import KnockoutRoundColumn from "../components/knockout/KnockoutRoundColumn.jsx";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { getKnockout } from "../services/knockoutService.js";
import "../App.css";

function tienePartidos(rounds) {
  return rounds.some(
    (round) => Array.isArray(round.matches) && round.matches.length > 0
  );
}

function KnockoutBracketPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [bracket, setBracket] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarLlave() {
      setCargando(true);
      setError("");

      try {
        setBracket(await getKnockout(tournamentId));
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarLlave();
  }, [navigate, tournamentId]);

  const rounds = Array.isArray(bracket?.rounds) ? bracket.rounds : [];
  const hasMatches = tienePartidos(rounds);

  return (
    <main className="ranking-page knockout-page">
      <section className="ranking-header knockout-header">
        <Badge variant="amber">Eliminatorias</Badge>
        <h1>{bracket?.tournamentName || "Llave eliminatoria"}</h1>
        <p>Rondas, cruces, resultados y ganadores del torneo.</p>

        <div className="standings-header-actions">
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
          <span>Cargando llave eliminatoria...</span>
        </div>
      )}

      {!cargando && <ErrorMessage message={error} />}

      {!cargando && !error && !hasMatches && (
        <EmptyState
          description="Cuando el administrador genere la llave, los cruces apareceran aca."
          title="Llave eliminatoria vacia"
        />
      )}

      {!cargando && !error && hasMatches && (
        <section className="knockout-bracket" aria-label="Llave eliminatoria">
          {rounds.map((round) => (
            <KnockoutRoundColumn key={round.round} round={round} />
          ))}
        </section>
      )}
    </main>
  );
}

export default KnockoutBracketPage;
