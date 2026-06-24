import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import MatchCard from "../components/match/MatchCard.jsx";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import {
  guardarPronostico,
  obtenerPartidos,
  obtenerTorneosDisponibles,
} from "../services/userTournamentService.js";
import "../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const estadoTorneoLabel = {
  ACTIVE: "Activo",
  FINISHED: "Finalizado",
};

const estadoTorneoVariant = {
  ACTIVE: "success",
  FINISHED: "primary",
};

function obtenerTimestamp(fecha) {
  const timestamp = new Date(fecha).getTime();
  return Number.isNaN(timestamp) ? Number.MAX_SAFE_INTEGER : timestamp;
}

function obtenerOrdenFecha(partido) {
  return (
    partido.matchDayOrderNumber ??
    partido.matchDayOrder ??
    partido.orderNumber ??
    partido.matchDayId ??
    Number.MAX_SAFE_INTEGER
  );
}

function agruparPorFecha(partidos) {
  const grupos = partidos.reduce((acc, partido) => {
    const key = partido.matchDayId || "sin-fecha";

    if (!acc[key]) {
      acc[key] = {
        id: key,
        name: partido.matchDayName || "Sin fecha asignada",
        order: obtenerOrdenFecha(partido),
        matches: [],
      };
    }

    acc[key].matches.push(partido);
    return acc;
  }, {});

  return Object.values(grupos)
    .map((grupo) => ({
      ...grupo,
      matches: grupo.matches.sort(
        (a, b) => obtenerTimestamp(a.startTime) - obtenerTimestamp(b.startTime)
      ),
    }))
    .sort((a, b) => {
      if (a.order !== b.order) {
        return a.order - b.order;
      }

      return String(a.name).localeCompare(String(b.name), "es");
    });
}

function normalizarPartido(partido, torneo) {
  return {
    ...partido,
    tournamentId: partido.tournamentId || torneo.id,
    tournamentName: torneo.name,
    tournamentFormat: torneo.format || "LEAGUE",
    tournamentStatus: torneo.status,
  };
}

function MatchesPage() {
  const navigate = useNavigate();
  const [torneos, setTorneos] = useState([]);
  const [torneosConPartidos, setTorneosConPartidos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [advertencia, setAdvertencia] = useState("");

  const resumen = useMemo(() => {
    const partidos = torneosConPartidos.flatMap((torneo) => torneo.matches);

    return {
      torneosConPartidos: torneosConPartidos.length,
      proximos: partidos.filter((partido) => partido.status === "POR_JUGARSE")
        .length,
      finalizados: partidos.filter((partido) => partido.status === "FINALIZADO")
        .length,
    };
  }, [torneosConPartidos]);

  useEffect(() => {
    let activo = true;

    async function cargarPartidos() {
      if (!localStorage.getItem("token")) {
        navigate("/login", { replace: true });
        return;
      }

      setCargando(true);
      setError("");
      setAdvertencia("");

      try {
        const torneosData = await obtenerTorneosDisponibles();
        const listaTorneos = Array.isArray(torneosData) ? torneosData : [];

        if (listaTorneos.length === 0) {
          if (activo) {
            setTorneos([]);
            setTorneosConPartidos([]);
          }
          return;
        }

        const resultados = await Promise.allSettled(
          listaTorneos.map(async (torneo) => {
            const partidosData = await obtenerPartidos(torneo.id);
            const partidos = Array.isArray(partidosData)
              ? partidosData
                  .filter(
                    (partido) =>
                      partido.tournamentId !== null &&
                      partido.tournamentId !== undefined
                  )
                  .map((partido) => normalizarPartido(partido, torneo))
              : [];

            return {
              ...torneo,
              matches: partidos,
              matchDayGroups: agruparPorFecha(partidos),
            };
          })
        );

        const cargados = resultados
          .filter((resultado) => resultado.status === "fulfilled")
          .map((resultado) => resultado.value);
        const errores = resultados.filter(
          (resultado) => resultado.status === "rejected"
        );

        if (activo) {
          setTorneos(listaTorneos);
          setTorneosConPartidos(
            cargados.filter((torneo) => torneo.matches.length > 0)
          );

          if (errores.length > 0) {
            setAdvertencia(
              "Algunos torneos no pudieron cargar sus partidos. Intentá nuevamente si falta información."
            );
          }

          if (cargados.length === 0 && errores.length > 0) {
            setError("No se pudieron cargar los partidos de los torneos.");
          }
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
        <header className="matches-header matches-header--central">
          <div>
            <Badge variant="primary">CENTRAL DE PARTIDOS</Badge>
            <h1>Partidos</h1>
            <p>
              Consultá los encuentros de todas las competencias y prepará tus
              pronósticos.
            </p>
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

        <section className="matches-summary" aria-label="Resumen de partidos">
          <Card className="matches-summary-card">
            <span>Torneos con partidos</span>
            <strong>{resumen.torneosConPartidos}</strong>
          </Card>
          <Card className="matches-summary-card">
            <span>Próximos</span>
            <strong>{resumen.proximos}</strong>
          </Card>
          <Card className="matches-summary-card">
            <span>Finalizados</span>
            <strong>{resumen.finalizados}</strong>
          </Card>
        </section>

        {cargando && (
          <div className="matches-loading" role="status">
            <Spinner size={28} />
            <span>Cargando partidos...</span>
          </div>
        )}

        {!cargando && error && <ErrorMessage message={error} />}
        {!cargando && advertencia && !error && (
          <p className="matches-warning" role="status">
            {advertencia}
          </p>
        )}

        {!cargando && !error && torneos.length === 0 && (
          <EmptyState
            description="Todavía no existen competencias habilitadas para consultar."
            title="No hay torneos disponibles"
          />
        )}

        {!cargando &&
          !error &&
          torneos.length > 0 &&
          torneosConPartidos.length === 0 && (
            <EmptyState
              description="Los torneos disponibles todavía no tienen encuentros programados."
              title="No hay partidos cargados"
            />
          )}

        {!cargando && !error && torneosConPartidos.length > 0 && (
          <section
            className="matches-tournament-list"
            aria-label="Partidos agrupados por torneo"
          >
            {torneosConPartidos.map((torneo) => (
              <Card className="matches-tournament-block" key={torneo.id}>
                <header className="matches-tournament-header">
                  <div>
                    <div className="matches-tournament-header__badges">
                      <Badge size="sm" variant="primary">
                        {formatoLabel[torneo.format || "LEAGUE"]}
                      </Badge>
                      <Badge
                        size="sm"
                        variant={estadoTorneoVariant[torneo.status] || "neutral"}
                      >
                        {estadoTorneoLabel[torneo.status] || "Estado no disponible"}
                      </Badge>
                    </div>
                    <h2>{torneo.name || "Torneo sin nombre"}</h2>
                  </div>
                  <span>{torneo.matches.length} partidos</span>
                </header>

                <div className="matches-matchday-list">
                  {torneo.matchDayGroups.map((grupo) => (
                    <section className="matches-matchday-group" key={grupo.id}>
                      <h3>{grupo.name}</h3>
                      <div className="matches-grid matches-grid--tournament">
                        {grupo.matches.map((partido) => (
                          <MatchCard
                            key={partido.id}
                            match={partido}
                            onSave={(matchId, homeGoals, awayGoals) =>
                              guardarPronostico(
                                partido.tournamentId,
                                matchId,
                                homeGoals,
                                awayGoals
                              )
                            }
                            predictionEnabled={partido.tournamentStatus === "ACTIVE"}
                            showTournamentBadge
                            tournamentName={partido.tournamentName}
                          />
                        ))}
                      </div>
                    </section>
                  ))}
                </div>
              </Card>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default MatchesPage;
