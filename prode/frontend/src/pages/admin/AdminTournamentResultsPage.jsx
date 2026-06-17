import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import { obtenerFechasDelTorneo } from "../../services/tournamentScheduleService.js";
import { obtenerTorneoPorId } from "../../services/tournamentService.js";
import {
  guardarResultado,
  obtenerPartidosParaResultados,
} from "../../services/tournamentResultService.js";
import "../../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const statusVariant = {
  POR_JUGARSE: "primary",
  EN_JUEGO: "amber",
  FINALIZADO: "success",
};

function leerSesion() {
  return {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
  };
}

function formatearFecha(fecha) {
  if (!fecha) {
    return "Fecha no disponible";
  }

  const date = new Date(fecha);

  if (Number.isNaN(date.getTime())) {
    return "Fecha no disponible";
  }

  return date.toLocaleString("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function tieneResultado(partido) {
  return partido.homeGoals !== null && partido.homeGoals !== undefined &&
    partido.awayGoals !== null && partido.awayGoals !== undefined;
}

function AdminTournamentResultsPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneo, setTorneo] = useState(null);
  const [fechas, setFechas] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [matchDayId, setMatchDayId] = useState("");
  const [status, setStatus] = useState("");
  const [resultados, setResultados] = useState({});
  const [cargando, setCargando] = useState(true);
  const [cargandoPartidos, setCargandoPartidos] = useState(false);
  const [guardandoId, setGuardandoId] = useState(null);
  const [confirmandoId, setConfirmandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const partidosAgrupados = useMemo(() => {
    const ordenFechas = fechas.reduce((acc, fecha, index) => {
      acc[fecha.id] = index;
      return acc;
    }, {});

    return Object.values(
      partidos.reduce((acc, partido) => {
        const key = partido.matchDayId;

        if (!acc[key]) {
          acc[key] = {
            matchDayId: key,
            matchDayName: partido.matchDayName || "Fecha sin nombre",
            order: ordenFechas[key] ?? 9999,
            matches: [],
          };
        }

        acc[key].matches.push(partido);
        return acc;
      }, {})
    ).sort((a, b) => a.order - b.order);
  }, [fechas, partidos]);

  const cargarPartidos = useCallback(async () => {
    setCargandoPartidos(true);
    setError("");

    try {
      const data = await obtenerPartidosParaResultados(tournamentId, {
        matchDayId,
        status,
      });
      const lista = Array.isArray(data) ? data : [];
      setPartidos(lista);
      setResultados(
        lista.reduce((acc, partido) => {
          acc[partido.id] = {
            homeGoals: tieneResultado(partido) ? String(partido.homeGoals) : "",
            awayGoals: tieneResultado(partido) ? String(partido.awayGoals) : "",
          };
          return acc;
        }, {})
      );
    } catch (error) {
      setError(error.message);
    } finally {
      setCargandoPartidos(false);
    }
  }, [matchDayId, status, tournamentId]);

  useEffect(() => {
    if (!sesion.token) {
      navigate("/login", { replace: true });
      return;
    }

    if (sesion.role !== "ADMIN") {
      navigate("/dashboard", { replace: true });
      return;
    }

    let activo = true;

    async function cargarContexto() {
      setCargando(true);
      setError("");

      try {
        const [torneoData, fechasData] = await Promise.all([
          obtenerTorneoPorId(tournamentId),
          obtenerFechasDelTorneo(tournamentId),
        ]);

        if (activo) {
          setTorneo(torneoData);
          setFechas(Array.isArray(fechasData) ? fechasData : []);
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

    cargarContexto();

    return () => {
      activo = false;
    };
  }, [navigate, sesion.role, sesion.token, tournamentId]);

  useEffect(() => {
    if (!sesion.token || sesion.role !== "ADMIN" || cargando) {
      return;
    }

    Promise.resolve().then(() => cargarPartidos());
  }, [cargando, cargarPartidos, sesion.role, sesion.token]);

  function actualizarResultado(matchId, campo, valor) {
    setMensaje("");
    setError("");
    setResultados((actual) => ({
      ...actual,
      [matchId]: {
        ...(actual[matchId] || {}),
        [campo]: valor,
      },
    }));
  }

  function validarResultado(matchId) {
    const resultado = resultados[matchId] || {};
    const homeGoals = Number(resultado.homeGoals);
    const awayGoals = Number(resultado.awayGoals);

    if (resultado.homeGoals === "" || resultado.awayGoals === "") {
      throw new Error("Los dos campos de goles son obligatorios.");
    }

    if (
      !Number.isInteger(homeGoals) ||
      !Number.isInteger(awayGoals) ||
      homeGoals < 0 ||
      awayGoals < 0
    ) {
      throw new Error("Los goles deben ser enteros mayores o iguales a 0.");
    }

    return { homeGoals, awayGoals };
  }

  async function ejecutarGuardado(partido) {
    setGuardandoId(partido.id);
    setMensaje("");
    setError("");

    try {
      const { homeGoals, awayGoals } = validarResultado(partido.id);
      await guardarResultado(tournamentId, partido.id, homeGoals, awayGoals);
      setConfirmandoId(null);
      setMensaje("Resultado guardado correctamente. Los puntos fueron recalculados.");
      await cargarPartidos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardandoId(null);
    }
  }

  function solicitarGuardado(partido) {
    if (tieneResultado(partido) && confirmandoId !== partido.id) {
      setConfirmandoId(partido.id);
      return;
    }

    Promise.resolve().then(() => ejecutarGuardado(partido));
  }

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-results-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="success">Resultados</Badge>
            <h1>{torneo?.name || "Resultados"}</h1>
            <p>Carga marcadores finales y recalcula los puntos del Prode.</p>
          </div>

          <div className="admin-header__actions">
            <Button
              onClick={() => navigate(`/admin/tournaments/${tournamentId}`)}
              variant="secondary"
            >
              Volver al torneo
            </Button>
          </div>
        </header>

        {cargando && (
          <div className="admin-loading" role="status">
            <Spinner size={28} />
            <span>Cargando resultados...</span>
          </div>
        )}

        {!cargando && torneo && (
          <>
            <Card className="admin-tournament-detail-card">
              <Badge size="sm" variant="primary">
                {formatoLabel[torneo.format || "LEAGUE"]}
              </Badge>
              <Badge size="sm" variant="neutral">
                {torneo.status}
              </Badge>
              <h2>Resultados</h2>
              <p>
                Si corregis un marcador existente, el backend recalcula los
                puntos de los pronosticos de ese partido.
              </p>
            </Card>

            <Card className="admin-results-filters">
              <div className="admin-field">
                <label htmlFor="resultMatchDay">Fecha</label>
                <select
                  id="resultMatchDay"
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

              <div className="admin-field">
                <label htmlFor="resultStatus">Estado</label>
                <select
                  id="resultStatus"
                  onChange={(evento) => setStatus(evento.target.value)}
                  value={status}
                >
                  <option value="">Todos</option>
                  <option value="POR_JUGARSE">Pendientes</option>
                  <option value="EN_JUEGO">En juego</option>
                  <option value="FINALIZADO">Finalizados</option>
                </select>
              </div>
            </Card>

            {mensaje && (
              <p className="admin-success" role="status">
                {mensaje}
              </p>
            )}

            <ErrorMessage message={error} />

            {cargandoPartidos && (
              <div className="admin-loading" role="status">
                <Spinner size={24} />
                <span>Cargando partidos...</span>
              </div>
            )}

            {!cargandoPartidos && partidos.length === 0 && (
              <EmptyState
                description="No hay partidos para los filtros seleccionados."
                title="Sin partidos"
              />
            )}

            {!cargandoPartidos && partidosAgrupados.length > 0 && (
              <section className="admin-results-groups">
                {partidosAgrupados.map((grupo) => (
                  <div className="admin-results-group" key={grupo.matchDayId}>
                    <h2>{grupo.matchDayName}</h2>

                    <div className="admin-results-list">
                      {grupo.matches.map((partido) => (
                        <Card className="admin-result-card" key={partido.id}>
                          <div className="admin-result-card__match">
                            <Badge
                              size="sm"
                              variant={statusVariant[partido.status] || "neutral"}
                            >
                              {partido.status}
                            </Badge>
                            <h3>
                              {partido.homeTeamName} vs {partido.awayTeamName}
                            </h3>
                            <p>{formatearFecha(partido.startTime)}</p>
                            {tieneResultado(partido) && (
                              <strong>
                                Resultado actual: {partido.homeGoals} -{" "}
                                {partido.awayGoals}
                              </strong>
                            )}
                          </div>

                          <div className="admin-result-card__form">
                            <div className="admin-field">
                              <label htmlFor={`homeGoals-${partido.id}`}>
                                {partido.homeTeamName}
                              </label>
                              <input
                                disabled={guardandoId === partido.id}
                                id={`homeGoals-${partido.id}`}
                                min={0}
                                onChange={(evento) =>
                                  actualizarResultado(
                                    partido.id,
                                    "homeGoals",
                                    evento.target.value
                                  )
                                }
                                type="number"
                                value={resultados[partido.id]?.homeGoals ?? ""}
                              />
                            </div>

                            <div className="admin-field">
                              <label htmlFor={`awayGoals-${partido.id}`}>
                                {partido.awayTeamName}
                              </label>
                              <input
                                disabled={guardandoId === partido.id}
                                id={`awayGoals-${partido.id}`}
                                min={0}
                                onChange={(evento) =>
                                  actualizarResultado(
                                    partido.id,
                                    "awayGoals",
                                    evento.target.value
                                  )
                                }
                                type="number"
                                value={resultados[partido.id]?.awayGoals ?? ""}
                              />
                            </div>

                            <Button
                              isLoading={guardandoId === partido.id}
                              onClick={() => solicitarGuardado(partido)}
                              type="button"
                              variant={tieneResultado(partido) ? "amber" : "primary"}
                            >
                              {guardandoId === partido.id
                                ? "Guardando..."
                                : tieneResultado(partido)
                                  ? "Corregir resultado"
                                  : "Guardar resultado"}
                            </Button>
                          </div>

                          {confirmandoId === partido.id && (
                            <div className="admin-team-confirm">
                              <p>
                                Confirmar correccion? Se recalcularan los puntos
                                de los pronosticos de este partido.
                              </p>
                              <div className="admin-team-confirm__actions">
                                <Button
                                  disabled={guardandoId === partido.id}
                                  onClick={() => setConfirmandoId(null)}
                                  type="button"
                                  variant="secondary"
                                >
                                  Cancelar
                                </Button>
                                <Button
                                  isLoading={guardandoId === partido.id}
                                  onClick={() => ejecutarGuardado(partido)}
                                  type="button"
                                  variant="danger"
                                >
                                  Confirmar correccion
                                </Button>
                              </div>
                            </div>
                          )}
                        </Card>
                      ))}
                    </div>
                  </div>
                ))}
              </section>
            )}
          </>
        )}

        {!cargando && !torneo && error && <ErrorMessage message={error} />}
      </section>
    </main>
  );
}

export default AdminTournamentResultsPage;
