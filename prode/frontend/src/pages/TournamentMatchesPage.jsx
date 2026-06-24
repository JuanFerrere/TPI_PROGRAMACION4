import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import PredictionForm from "../components/match/PredictionForm.jsx";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import {
  guardarPronostico,
  obtenerMisPronosticos,
  obtenerPartidos,
  obtenerTorneo,
} from "../services/userTournamentService.js";
import "../App.css";

const statusVariant = {
  POR_JUGARSE: "primary",
  EN_JUEGO: "amber",
  FINALIZADO: "success",
};

const statusLabel = {
  POR_JUGARSE: "Por jugarse",
  EN_JUEGO: "En juego",
  FINALIZADO: "Finalizado",
};

function formatearFecha(fecha) {
  if (!fecha) {
    return "Fecha a confirmar";
  }

  const date = new Date(fecha);

  if (Number.isNaN(date.getTime())) {
    return "Fecha a confirmar";
  }

  return date.toLocaleString("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function cargaPronosticoCerrada(fecha) {
  if (!fecha) {
    return false;
  }

  const startTime = new Date(fecha).getTime();

  if (Number.isNaN(startTime)) {
    return false;
  }

  return startTime - Date.now() <= 30 * 60 * 1000;
}

function tieneResultado(partido) {
  return (
    partido.homeGoals !== null &&
    partido.homeGoals !== undefined &&
    partido.awayGoals !== null &&
    partido.awayGoals !== undefined
  );
}

function obtenerResultado(partido) {
  if (!tieneResultado(partido)) {
    return "-";
  }

  return `${partido.homeGoals} - ${partido.awayGoals}`;
}

function obtenerClasePartido({ cerrado, finalizado, puedePronosticar }) {
  if (finalizado) {
    return "finished";
  }

  if (cerrado || !puedePronosticar) {
    return "closed";
  }

  return "upcoming";
}

function TournamentMatchesPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [torneo, setTorneo] = useState(null);
  const [todosLosPartidos, setTodosLosPartidos] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [pronosticos, setPronosticos] = useState([]);
  const [matchDayId, setMatchDayId] = useState("");
  const [formularioAbiertoId, setFormularioAbiertoId] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  const pronosticosPorPartido = useMemo(
    () =>
      pronosticos.reduce((acc, prediction) => {
        acc[prediction.matchId] = prediction;
        return acc;
      }, {}),
    [pronosticos],
  );

  const fechas = useMemo(() => {
    const map = todosLosPartidos.reduce((acc, partido) => {
      if (!acc[partido.matchDayId]) {
        acc[partido.matchDayId] = partido.matchDayName || "Fecha sin nombre";
      }
      return acc;
    }, {});

    return Object.entries(map).map(([id, name]) => ({ id, name }));
  }, [todosLosPartidos]);

  const fechaSeleccionada =
    fechas.find((fecha) => fecha.id === matchDayId)?.name || "Todas";

  const resumen = useMemo(() => {
    const total = partidos.length;
    const porJugarse = partidos.filter(
      (partido) => partido.status === "POR_JUGARSE",
    ).length;
    const finalizados = partidos.filter(
      (partido) => partido.status === "FINALIZADO",
    ).length;

    return { finalizados, porJugarse, total };
  }, [partidos]);

  const cargarPartidosYPronosticos = useCallback(async () => {
    const [partidosData, pronosticosData] = await Promise.all([
      obtenerPartidos(tournamentId, matchDayId),
      obtenerMisPronosticos(tournamentId),
    ]);

    setPartidos(Array.isArray(partidosData) ? partidosData : []);
    setPronosticos(Array.isArray(pronosticosData) ? pronosticosData : []);
  }, [matchDayId, tournamentId]);

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarDatos() {
      setCargando(true);
      setError("");

      try {
        const [torneoData, partidosData, pronosticosData] = await Promise.all([
          obtenerTorneo(tournamentId),
          obtenerPartidos(tournamentId),
          obtenerMisPronosticos(tournamentId),
        ]);
        setTorneo(torneoData);
        setTodosLosPartidos(Array.isArray(partidosData) ? partidosData : []);
        setPartidos(Array.isArray(partidosData) ? partidosData : []);
        setPronosticos(Array.isArray(pronosticosData) ? pronosticosData : []);
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarDatos();
  }, [navigate, tournamentId]);

  useEffect(() => {
    if (cargando) {
      return;
    }

    Promise.resolve()
      .then(() => cargarPartidosYPronosticos())
      .catch((error) => setError(error.message));
  }, [cargando, cargarPartidosYPronosticos]);

  async function recargarLuegoDeGuardar() {
    setFormularioAbiertoId(null);
    await cargarPartidosYPronosticos();
  }

  return (
    <main className="matches-page tournament-matches-page">
      <section className="matches-shell tournament-matches-shell">
        <header className="matches-header tournament-matches-header">
          <div>
            <Badge variant="primary">FIXTURE DEL TORNEO</Badge>
            <h1>{torneo?.name || "Partidos"}</h1>
            <p>Partidos y pronósticos exclusivos de esta competencia.</p>
          </div>

          <div className="matches-header__actions">
            <Button
              onClick={() => navigate(`/tournaments/${tournamentId}`)}
              variant="secondary"
            >
              Volver al torneo
            </Button>
            <Button
              onClick={() => navigate(`/tournaments/${tournamentId}/predictions`)}
              variant="secondary"
            >
              Mis pronósticos
            </Button>
          </div>
        </header>

        {cargando && (
          <div className="matches-loading tournament-matches-loading" role="status">
            <Spinner size={28} />
            <span>Cargando partidos...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && (
          <>
            <Card className="tournament-matches-hero">
              <div>
                <Badge size="sm" variant="primary">
                  Central interna
                </Badge>
                <h2>{torneo?.name || "Torneo"}</h2>
                <p>
                  Fecha seleccionada: <strong>{fechaSeleccionada}</strong>
                </p>
              </div>

              <div className="tournament-matches-summary">
                <div>
                  <span>Partidos visibles</span>
                  <strong>{resumen.total}</strong>
                </div>
                <div>
                  <span>Por jugarse</span>
                  <strong>{resumen.porJugarse}</strong>
                </div>
                <div>
                  <span>Finalizados</span>
                  <strong>{resumen.finalizados}</strong>
                </div>
              </div>

              <div className="tournament-matches-journey" aria-label="Recorrido">
                {["Ver partidos", "Cargar pronóstico", "Esperar resultado", "Revisar puntos"].map(
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

            <Card className="tournament-filter-card tournament-matches-filter-card">
              <div className="admin-field tournament-matches-filter-card__field">
                <label htmlFor="tournamentMatchDay">Filtrar por fecha</label>
                <select
                  id="tournamentMatchDay"
                  onChange={(evento) => setMatchDayId(evento.target.value)}
                  value={matchDayId}
                >
                  <option value="">Todas</option>
                  {fechas.map((fecha) => (
                    <option key={fecha.id} value={fecha.id}>
                      {fecha.name}
                    </option>
                  ))}
                </select>
              </div>
            </Card>

            {partidos.length === 0 && (
              <EmptyState
                description={
                  todosLosPartidos.length === 0
                    ? "Todavía no existen encuentros programados para este torneo."
                    : "No hay encuentros programados para la fecha seleccionada."
                }
                title={
                  todosLosPartidos.length === 0
                    ? "No hay partidos cargados"
                    : "No hay partidos para esta fecha"
                }
              />
            )}

            {partidos.length > 0 && (
              <section
                className="tournament-matches-grid"
                aria-label="Partidos del torneo"
              >
                {partidos.map((partido) => {
                  const prediction = pronosticosPorPartido[partido.id];
                  const finalizado = partido.status === "FINALIZADO";
                  const cerrado = cargaPronosticoCerrada(partido.startTime);
                  const puedePronosticar =
                    torneo?.status === "ACTIVE" &&
                    partido.status === "POR_JUGARSE" &&
                    !cerrado;
                  const estadoVisual = statusLabel[partido.status] || partido.status;
                  const clasePartido = obtenerClasePartido({
                    cerrado,
                    finalizado,
                    puedePronosticar,
                  });

                  return (
                    <Card
                      className={`tournament-match-card tournament-match-card--${clasePartido}`}
                      key={partido.id}
                    >
                      <div className="tournament-match-card__top">
                        <span>{partido.matchDayName || "Fecha sin nombre"}</span>
                        <Badge
                          size="sm"
                          variant={statusVariant[partido.status] || "neutral"}
                        >
                          {estadoVisual}
                        </Badge>
                      </div>

                      <div className="tournament-match-card__teams">
                        <strong>{partido.homeTeamName || "Local"}</strong>
                        <span>{finalizado ? obtenerResultado(partido) : "VS"}</span>
                        <strong>{partido.awayTeamName || "Visitante"}</strong>
                      </div>

                      <div className="tournament-match-card__info">
                        <div>
                          <span>Fecha y hora</span>
                          <strong>{formatearFecha(partido.startTime)}</strong>
                        </div>
                        <div>
                          <span>{finalizado ? "Resultado final" : "Resultado pendiente"}</span>
                          <strong>
                            {tieneResultado(partido) ? obtenerResultado(partido) : "-"}
                          </strong>
                        </div>
                      </div>

                      {prediction && (
                        <div className="tournament-match-card__prediction">
                          <span>Tu pronóstico</span>
                          <strong>
                            {prediction.predictedHomeGoals} -{" "}
                            {prediction.predictedAwayGoals}
                          </strong>
                          {finalizado && (
                            <small>{prediction.points ?? 0} puntos</small>
                          )}
                        </div>
                      )}

                      {partido.status === "POR_JUGARSE" && !puedePronosticar && (
                        <p className="tournament-match-card__closed">
                          {torneo?.status !== "ACTIVE"
                            ? "Este torneo no permite nuevos pronósticos."
                            : "La carga de pronósticos ya está cerrada."}
                        </p>
                      )}

                      {puedePronosticar && formularioAbiertoId !== partido.id && (
                        <Button
                          fullWidth
                          onClick={() => setFormularioAbiertoId(partido.id)}
                          variant="secondary"
                        >
                          Pronosticar
                        </Button>
                      )}

                      {puedePronosticar && formularioAbiertoId === partido.id && (
                        <PredictionForm
                          match={partido}
                          onCancel={() => setFormularioAbiertoId(null)}
                          onSave={(matchId, homeGoals, awayGoals) =>
                            guardarPronostico(
                              tournamentId,
                              matchId,
                              homeGoals,
                              awayGoals,
                            )
                          }
                          onSaved={recargarLuegoDeGuardar}
                        />
                      )}
                    </Card>
                  );
                })}
              </section>
            )}
          </>
        )}
      </section>
    </main>
  );
}

export default TournamentMatchesPage;
