import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import {
  obtenerEquiposTorneo,
  obtenerTorneoPorId,
} from "../../services/tournamentService.js";
import {
  crearFecha,
  crearFechasMasivamente,
  crearPartido,
  crearPartidosMasivamente,
  eliminarFecha,
  eliminarPartido,
  obtenerFechasDelTorneo,
  obtenerPartidosDelTorneo,
} from "../../services/tournamentScheduleService.js";
import "../../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
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

function parseDatetimeLocal(value) {
  if (!value) {
    return null;
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return date.toISOString();
}

function ordenarEquipos(equipos, isGroups) {
  return [...equipos].sort((a, b) => {
    if (isGroups) {
      const groupCompare = (a.groupName || "").localeCompare(
        b.groupName || "",
        "es"
      );

      if (groupCompare !== 0) {
        return groupCompare;
      }
    }

    return (a.teamName || "").localeCompare(b.teamName || "", "es");
  });
}

function AdminTournamentSchedulePage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneo, setTorneo] = useState(null);
  const [equipos, setEquipos] = useState([]);
  const [fechas, setFechas] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [selectedMatchDayId, setSelectedMatchDayId] = useState("");
  const [fechaNombre, setFechaNombre] = useState("");
  const [fechaOrden, setFechaOrden] = useState("");
  const [fechasBulk, setFechasBulk] = useState("");
  const [homeTournamentTeamId, setHomeTournamentTeamId] = useState("");
  const [awayTournamentTeamId, setAwayTournamentTeamId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [partidosBulk, setPartidosBulk] = useState("");
  const [cargando, setCargando] = useState(true);
  const [guardandoFecha, setGuardandoFecha] = useState(false);
  const [guardandoFechasBulk, setGuardandoFechasBulk] = useState(false);
  const [guardandoPartido, setGuardandoPartido] = useState(false);
  const [guardandoPartidosBulk, setGuardandoPartidosBulk] = useState(false);
  const [confirmandoFechaId, setConfirmandoFechaId] = useState(null);
  const [eliminandoFechaId, setEliminandoFechaId] = useState(null);
  const [confirmandoPartidoId, setConfirmandoPartidoId] = useState(null);
  const [eliminandoPartidoId, setEliminandoPartidoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const isGroups = (torneo?.format || "LEAGUE") === "GROUPS";
  const equiposOrdenados = useMemo(
    () => ordenarEquipos(equipos, isGroups),
    [equipos, isGroups]
  );

  const partidosDeFecha = useMemo(
    () =>
      partidos.filter(
        (partido) => String(partido.matchDayId) === String(selectedMatchDayId)
      ),
    [partidos, selectedMatchDayId]
  );

  const conteoPorFecha = useMemo(
    () =>
      partidos.reduce((acc, partido) => {
        acc[partido.matchDayId] = (acc[partido.matchDayId] || 0) + 1;
        return acc;
      }, {}),
    [partidos]
  );

  const fechasBulkPreview = useMemo(() => {
    const errors = [];
    const matchDays = [];

    fechasBulk
      .split(/\r?\n/)
      .map((line) => line.trim())
      .forEach((line, index) => {
        if (!line) {
          return;
        }

        const parts = line.split("|");

        if (parts.length !== 2) {
          errors.push(`Linea ${index + 1}: usar formato Fecha|Orden`);
          return;
        }

        const name = parts[0].trim();
        const orderNumber = Number(parts[1].trim());

        if (!name) {
          errors.push(`Linea ${index + 1}: nombre obligatorio`);
          return;
        }

        if (!Number.isInteger(orderNumber) || orderNumber <= 0) {
          errors.push(`Linea ${index + 1}: orden invalido`);
          return;
        }

        matchDays.push({ name, orderNumber });
      });

    return { errors, matchDays };
  }, [fechasBulk]);

  const partidosBulkPreview = useMemo(() => {
    const errors = [];
    const matches = [];
    const teamsByName = equipos.reduce((acc, equipo) => {
      acc[(equipo.teamName || "").trim().toLowerCase()] = equipo;
      return acc;
    }, {});

    partidosBulk
      .split(/\r?\n/)
      .map((line) => line.trim())
      .forEach((line, index) => {
        if (!line) {
          return;
        }

        const parts = line.split("|");

        if (parts.length !== 3) {
          errors.push(`Linea ${index + 1}: usar formato Local|Visitante|FechaHora`);
          return;
        }

        const homeName = parts[0].trim();
        const awayName = parts[1].trim();
        const isoDate = parseDatetimeLocal(parts[2].trim());
        const homeTeam = teamsByName[homeName.toLowerCase()];
        const awayTeam = teamsByName[awayName.toLowerCase()];

        if (!homeTeam) {
          errors.push(`Linea ${index + 1}: no existe el equipo local ${homeName}`);
          return;
        }

        if (!awayTeam) {
          errors.push(`Linea ${index + 1}: no existe el equipo visitante ${awayName}`);
          return;
        }

        if (homeTeam.id === awayTeam.id) {
          errors.push(`Linea ${index + 1}: local y visitante no pueden ser iguales`);
          return;
        }

        if (!isoDate) {
          errors.push(`Linea ${index + 1}: fecha y hora invalida`);
          return;
        }

        matches.push({
          homeTournamentTeamId: homeTeam.id,
          awayTournamentTeamId: awayTeam.id,
          startTime: isoDate,
        });
      });

    return { errors, matches };
  }, [equipos, partidosBulk]);

  const cargarDatos = useCallback(async () => {
    setCargando(true);
    setError("");

    try {
      const [torneoData, equiposData, fechasData, partidosData] =
        await Promise.all([
          obtenerTorneoPorId(tournamentId),
          obtenerEquiposTorneo(tournamentId),
          obtenerFechasDelTorneo(tournamentId),
          obtenerPartidosDelTorneo(tournamentId),
        ]);
      const listaFechas = Array.isArray(fechasData) ? fechasData : [];
      setTorneo(torneoData);
      setEquipos(Array.isArray(equiposData) ? equiposData : []);
      setFechas(listaFechas);
      setPartidos(Array.isArray(partidosData) ? partidosData : []);
      setSelectedMatchDayId((actual) => {
        if (actual && listaFechas.some((fecha) => String(fecha.id) === String(actual))) {
          return actual;
        }

        return listaFechas[0]?.id ? String(listaFechas[0].id) : "";
      });
    } catch (error) {
      setError(error.message);
    } finally {
      setCargando(false);
    }
  }, [tournamentId]);

  useEffect(() => {
    if (!sesion.token) {
      navigate("/login", { replace: true });
      return;
    }

    if (sesion.role !== "ADMIN") {
      navigate("/dashboard", { replace: true });
      return;
    }

    Promise.resolve().then(() => cargarDatos());
  }, [cargarDatos, navigate, sesion.role, sesion.token]);

  async function manejarCrearFecha(evento) {
    evento.preventDefault();
    const name = fechaNombre.trim();
    const orderNumber = fechaOrden ? Number(fechaOrden) : null;

    if (!name) {
      setError("El nombre de la fecha es obligatorio.");
      return;
    }

    if (orderNumber !== null && (!Number.isInteger(orderNumber) || orderNumber <= 0)) {
      setError("El numero de orden debe ser positivo.");
      return;
    }

    setGuardandoFecha(true);
    setMensaje("");
    setError("");

    try {
      await crearFecha(tournamentId, name, orderNumber);
      setFechaNombre("");
      setFechaOrden("");
      setMensaje("Fecha creada correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardandoFecha(false);
    }
  }

  async function manejarCrearFechasBulk(evento) {
    evento.preventDefault();

    if (fechasBulkPreview.errors.length > 0) {
      setError(fechasBulkPreview.errors.join(" | "));
      return;
    }

    if (fechasBulkPreview.matchDays.length === 0) {
      setError("No hay fechas validas para cargar.");
      return;
    }

    setGuardandoFechasBulk(true);
    setMensaje("");
    setError("");

    try {
      await crearFechasMasivamente(tournamentId, fechasBulkPreview.matchDays);
      setFechasBulk("");
      setMensaje("Fechas cargadas correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardandoFechasBulk(false);
    }
  }

  async function manejarCrearPartido(evento) {
    evento.preventDefault();

    if (!selectedMatchDayId) {
      setError("Selecciona una fecha para crear partidos.");
      return;
    }

    if (!homeTournamentTeamId || !awayTournamentTeamId) {
      setError("Selecciona equipo local y visitante.");
      return;
    }

    if (homeTournamentTeamId === awayTournamentTeamId) {
      setError("Local y visitante no pueden ser el mismo equipo.");
      return;
    }

    const isoDate = parseDatetimeLocal(startTime);

    if (!isoDate) {
      setError("La fecha y hora del partido es obligatoria.");
      return;
    }

    setGuardandoPartido(true);
    setMensaje("");
    setError("");

    try {
      await crearPartido(tournamentId, {
        matchDayId: Number(selectedMatchDayId),
        homeTournamentTeamId: Number(homeTournamentTeamId),
        awayTournamentTeamId: Number(awayTournamentTeamId),
        startTime: isoDate,
      });
      setHomeTournamentTeamId("");
      setAwayTournamentTeamId("");
      setStartTime("");
      setMensaje("Partido creado correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardandoPartido(false);
    }
  }

  async function manejarCrearPartidosBulk(evento) {
    evento.preventDefault();

    if (!selectedMatchDayId) {
      setError("Selecciona una fecha para cargar partidos.");
      return;
    }

    if (partidosBulkPreview.errors.length > 0) {
      setError(partidosBulkPreview.errors.join(" | "));
      return;
    }

    if (partidosBulkPreview.matches.length === 0) {
      setError("No hay partidos validos para cargar.");
      return;
    }

    setGuardandoPartidosBulk(true);
    setMensaje("");
    setError("");

    try {
      await crearPartidosMasivamente(tournamentId, {
        matchDayId: Number(selectedMatchDayId),
        matches: partidosBulkPreview.matches,
      });
      setPartidosBulk("");
      setMensaje("Partidos cargados correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardandoPartidosBulk(false);
    }
  }

  async function confirmarEliminarFecha(matchDayId) {
    setEliminandoFechaId(matchDayId);
    setMensaje("");
    setError("");

    try {
      await eliminarFecha(tournamentId, matchDayId);
      setConfirmandoFechaId(null);
      setMensaje("Fecha eliminada correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setEliminandoFechaId(null);
    }
  }

  async function confirmarEliminarPartido(matchId) {
    setEliminandoPartidoId(matchId);
    setMensaje("");
    setError("");

    try {
      await eliminarPartido(tournamentId, matchId);
      setConfirmandoPartidoId(null);
      setMensaje("Partido eliminado correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setEliminandoPartidoId(null);
    }
  }

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-schedule-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="success">Fechas y partidos</Badge>
            <h1>{torneo?.name || "Fechas y partidos"}</h1>
            <p>Organiza las jornadas y carga los encuentros del torneo.</p>
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
            <span>Cargando fixture...</span>
          </div>
        )}

        {!cargando && torneo && (
          <>
            <Card className="admin-tournament-detail-card">
              <Badge size="sm" variant={isGroups ? "amber" : "primary"}>
                {formatoLabel[torneo.format || "LEAGUE"]}
              </Badge>
              <h2>Fechas y partidos</h2>
              <p>
                {isGroups
                  ? "Los equipos muestran grupo como referencia, pero se permiten cruces entre grupos."
                  : "El torneo usa una lista general de equipos."}
              </p>
            </Card>

            {mensaje && (
              <p className="admin-success" role="status">
                {mensaje}
              </p>
            )}

            <ErrorMessage message={error} />

            <section className="admin-schedule-layout">
              <div className="admin-schedule-column">
                <Card className="admin-tournament-form-card">
                  <h2>Fechas</h2>
                  <form className="admin-schedule-form" onSubmit={manejarCrearFecha}>
                    <div className="admin-field">
                      <label htmlFor="matchDayName">Nombre</label>
                      <input
                        disabled={guardandoFecha}
                        id="matchDayName"
                        maxLength={100}
                        onChange={(evento) => setFechaNombre(evento.target.value)}
                        required
                        type="text"
                        value={fechaNombre}
                      />
                    </div>

                    <div className="admin-field">
                      <label htmlFor="matchDayOrder">Numero de orden</label>
                      <input
                        disabled={guardandoFecha}
                        id="matchDayOrder"
                        min={1}
                        onChange={(evento) => setFechaOrden(evento.target.value)}
                        type="number"
                        value={fechaOrden}
                      />
                    </div>

                    <Button isLoading={guardandoFecha} type="submit">
                      {guardandoFecha ? "Creando..." : "Crear fecha"}
                    </Button>
                  </form>
                </Card>

                <Card className="admin-tournament-form-card">
                  <form className="admin-schedule-form" onSubmit={manejarCrearFechasBulk}>
                    <div className="admin-field">
                      <label htmlFor="matchDaysBulk">Carga masiva</label>
                      <textarea
                        disabled={guardandoFechasBulk}
                        id="matchDaysBulk"
                        onChange={(evento) => setFechasBulk(evento.target.value)}
                        placeholder={"Fecha 1|1\nFecha 2|2\nFecha 3|3"}
                        rows={5}
                        value={fechasBulk}
                      />
                    </div>

                    <p className="admin-schedule-hint">
                      {fechasBulkPreview.matchDays.length} fechas validas detectadas
                    </p>

                    {fechasBulkPreview.errors.length > 0 && (
                      <ul className="admin-schedule-errors">
                        {fechasBulkPreview.errors.map((previewError) => (
                          <li key={previewError}>{previewError}</li>
                        ))}
                      </ul>
                    )}

                    <Button isLoading={guardandoFechasBulk} type="submit" variant="secondary">
                      {guardandoFechasBulk ? "Cargando..." : "Cargar fechas"}
                    </Button>
                  </form>
                </Card>

                {fechas.length === 0 ? (
                  <EmptyState
                    description="Cuando crees fechas, van a aparecer aca."
                    title="No hay fechas cargadas"
                  />
                ) : (
                  <section className="admin-schedule-list">
                    {fechas.map((fecha) => (
                      <Card
                        className={`admin-schedule-matchday-card ${
                          String(selectedMatchDayId) === String(fecha.id)
                            ? "admin-schedule-matchday-card--active"
                            : ""
                        }`}
                        key={fecha.id}
                      >
                        <div>
                          <Badge size="sm" variant="neutral">
                            Orden {fecha.orderNumber || "-"}
                          </Badge>
                          <h3>{fecha.name}</h3>
                          <p>{conteoPorFecha[fecha.id] || 0} partidos</p>
                        </div>

                        <div className="admin-schedule-card-actions">
                          <Button
                            onClick={() => setSelectedMatchDayId(String(fecha.id))}
                            type="button"
                            variant="secondary"
                          >
                            Ver partidos
                          </Button>

                          {confirmandoFechaId === fecha.id ? (
                            <div className="admin-team-confirm">
                              <p>Eliminar esta fecha?</p>
                              <div className="admin-team-confirm__actions">
                                <Button
                                  disabled={eliminandoFechaId === fecha.id}
                                  onClick={() => setConfirmandoFechaId(null)}
                                  type="button"
                                  variant="secondary"
                                >
                                  Cancelar
                                </Button>
                                <Button
                                  isLoading={eliminandoFechaId === fecha.id}
                                  onClick={() => confirmarEliminarFecha(fecha.id)}
                                  type="button"
                                  variant="danger"
                                >
                                  Confirmar
                                </Button>
                              </div>
                            </div>
                          ) : (
                            <Button
                              disabled={eliminandoFechaId !== null}
                              onClick={() => setConfirmandoFechaId(fecha.id)}
                              type="button"
                              variant="danger"
                            >
                              Eliminar
                            </Button>
                          )}
                        </div>
                      </Card>
                    ))}
                  </section>
                )}
              </div>

              <div className="admin-schedule-column">
                <Card className="admin-tournament-form-card">
                  <h2>Partidos de la fecha seleccionada</h2>

                  {!selectedMatchDayId ? (
                    <p className="admin-schedule-hint">
                      Crea o selecciona una fecha para cargar partidos.
                    </p>
                  ) : (
                    <form className="admin-schedule-form" onSubmit={manejarCrearPartido}>
                      <div className="admin-schedule-two-columns">
                        <div className="admin-field">
                          <label htmlFor="homeTeam">Equipo local</label>
                          <select
                            disabled={guardandoPartido}
                            id="homeTeam"
                            onChange={(evento) =>
                              setHomeTournamentTeamId(evento.target.value)
                            }
                            value={homeTournamentTeamId}
                          >
                            <option value="">Seleccionar</option>
                            {equiposOrdenados.map((equipo) => (
                              <option
                                disabled={String(equipo.id) === String(awayTournamentTeamId)}
                                key={equipo.id}
                                value={equipo.id}
                              >
                                {isGroups && equipo.groupName
                                  ? `Grupo ${equipo.groupName} - ${equipo.teamName}`
                                  : equipo.teamName}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div className="admin-field">
                          <label htmlFor="awayTeam">Equipo visitante</label>
                          <select
                            disabled={guardandoPartido}
                            id="awayTeam"
                            onChange={(evento) =>
                              setAwayTournamentTeamId(evento.target.value)
                            }
                            value={awayTournamentTeamId}
                          >
                            <option value="">Seleccionar</option>
                            {equiposOrdenados.map((equipo) => (
                              <option
                                disabled={String(equipo.id) === String(homeTournamentTeamId)}
                                key={equipo.id}
                                value={equipo.id}
                              >
                                {isGroups && equipo.groupName
                                  ? `Grupo ${equipo.groupName} - ${equipo.teamName}`
                                  : equipo.teamName}
                              </option>
                            ))}
                          </select>
                        </div>
                      </div>

                      <div className="admin-field">
                        <label htmlFor="matchStartTime">Fecha y hora</label>
                        <input
                          disabled={guardandoPartido}
                          id="matchStartTime"
                          onChange={(evento) => setStartTime(evento.target.value)}
                          type="datetime-local"
                          value={startTime}
                        />
                      </div>

                      <Button isLoading={guardandoPartido} type="submit">
                        {guardandoPartido ? "Creando..." : "Crear partido"}
                      </Button>
                    </form>
                  )}
                </Card>

                {selectedMatchDayId && (
                  <Card className="admin-tournament-form-card">
                    <form className="admin-schedule-form" onSubmit={manejarCrearPartidosBulk}>
                      <div className="admin-field">
                        <label htmlFor="matchesBulk">Carga masiva de partidos</label>
                        <textarea
                          disabled={guardandoPartidosBulk}
                          id="matchesBulk"
                          onChange={(evento) => setPartidosBulk(evento.target.value)}
                          placeholder={
                            "Argentina|Argelia|2026-06-20T16:00\nAustria|Jordania|2026-06-20T19:00"
                          }
                          rows={5}
                          value={partidosBulk}
                        />
                      </div>

                      <p className="admin-schedule-hint">
                        {partidosBulkPreview.matches.length} partidos validos detectados
                      </p>

                      {partidosBulkPreview.errors.length > 0 && (
                        <ul className="admin-schedule-errors">
                          {partidosBulkPreview.errors.map((previewError) => (
                            <li key={previewError}>{previewError}</li>
                          ))}
                        </ul>
                      )}

                      <Button isLoading={guardandoPartidosBulk} type="submit" variant="secondary">
                        {guardandoPartidosBulk ? "Cargando..." : "Cargar partidos"}
                      </Button>
                    </form>
                  </Card>
                )}

                {selectedMatchDayId && partidosDeFecha.length === 0 && (
                  <EmptyState
                    description="Cuando cargues partidos para esta fecha, van a aparecer aca."
                    title="No hay partidos en esta fecha"
                  />
                )}

                {selectedMatchDayId && partidosDeFecha.length > 0 && (
                  <section className="admin-schedule-list">
                    {partidosDeFecha.map((partido) => (
                      <Card className="admin-schedule-match-card" key={partido.id}>
                        <div className="admin-schedule-match-card__teams">
                          <strong>
                            {partido.homeTeamName} vs {partido.awayTeamName}
                          </strong>
                          {isGroups && (
                            <span>
                              {partido.homeGroupName || "-"} /{" "}
                              {partido.awayGroupName || "-"}
                            </span>
                          )}
                          <p>{formatearFecha(partido.startTime)}</p>
                        </div>

                        <Badge size="sm" variant="neutral">
                          {partido.status}
                        </Badge>

                        {confirmandoPartidoId === partido.id ? (
                          <div className="admin-team-confirm">
                            <p>Eliminar este partido?</p>
                            <div className="admin-team-confirm__actions">
                              <Button
                                disabled={eliminandoPartidoId === partido.id}
                                onClick={() => setConfirmandoPartidoId(null)}
                                type="button"
                                variant="secondary"
                              >
                                Cancelar
                              </Button>
                              <Button
                                isLoading={eliminandoPartidoId === partido.id}
                                onClick={() => confirmarEliminarPartido(partido.id)}
                                type="button"
                                variant="danger"
                              >
                                Confirmar
                              </Button>
                            </div>
                          </div>
                        ) : (
                          <Button
                            disabled={eliminandoPartidoId !== null}
                            onClick={() => setConfirmandoPartidoId(partido.id)}
                            type="button"
                            variant="danger"
                          >
                            Eliminar
                          </Button>
                        )}
                      </Card>
                    ))}
                  </section>
                )}
              </div>
            </section>
          </>
        )}

        {!cargando && !torneo && error && <ErrorMessage message={error} />}
      </section>
    </main>
  );
}

export default AdminTournamentSchedulePage;
