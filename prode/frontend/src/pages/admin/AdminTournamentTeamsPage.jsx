import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import {
  actualizarGrupoEquipoTorneo,
  agregarEquipoTorneo,
  agregarEquiposTorneoMasivo,
  eliminarEquipoTorneo,
  obtenerEquiposTorneo,
  obtenerTorneoPorId,
} from "../../services/tournamentService.js";
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

function agruparEquipos(equipos, format) {
  if (format !== "GROUPS") {
    return [{ groupName: "Tabla general", teams: equipos }];
  }

  const groups = equipos.reduce((acc, equipo) => {
    const groupName = equipo.groupName || "SIN GRUPO";

    if (!acc[groupName]) {
      acc[groupName] = [];
    }

    acc[groupName].push(equipo);
    return acc;
  }, {});

  return Object.keys(groups)
    .sort((a, b) => a.localeCompare(b, "es"))
    .map((groupName) => ({
      groupName: `Grupo ${groupName}`,
      rawGroupName: groupName,
      teams: groups[groupName],
    }));
}

function AdminTournamentTeamsPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneo, setTorneo] = useState(null);
  const [equipos, setEquipos] = useState([]);
  const [name, setName] = useState("");
  const [groupName, setGroupName] = useState("");
  const [bulkContent, setBulkContent] = useState("");
  const [groupEdits, setGroupEdits] = useState({});
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [guardandoMasivo, setGuardandoMasivo] = useState(false);
  const [actualizandoGrupoId, setActualizandoGrupoId] = useState(null);
  const [confirmandoId, setConfirmandoId] = useState(null);
  const [eliminandoId, setEliminandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const isGroups = (torneo?.format || "LEAGUE") === "GROUPS";
  const equiposAgrupados = useMemo(
    () => agruparEquipos(equipos, torneo?.format || "LEAGUE"),
    [equipos, torneo?.format]
  );

  const cargarDatos = useCallback(async () => {
    setCargando(true);
    setError("");

    try {
      const [torneoData, equiposData] = await Promise.all([
        obtenerTorneoPorId(tournamentId),
        obtenerEquiposTorneo(tournamentId),
      ]);
      const listaEquipos = Array.isArray(equiposData) ? equiposData : [];
      setTorneo(torneoData);
      setEquipos(listaEquipos);
      setGroupEdits(
        listaEquipos.reduce((acc, equipo) => {
          acc[equipo.id] = equipo.groupName || "";
          return acc;
        }, {})
      );
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

  async function manejarAgregarEquipo(evento) {
    evento.preventDefault();

    const nombreNormalizado = name.trim();
    const grupoNormalizado = groupName.trim();

    if (!nombreNormalizado) {
      setError("El nombre del equipo es obligatorio.");
      return;
    }

    if (isGroups && !grupoNormalizado) {
      setError("El grupo es obligatorio para torneos por grupos.");
      return;
    }

    setGuardando(true);
    setMensaje("");
    setError("");

    try {
      await agregarEquipoTorneo(
        tournamentId,
        nombreNormalizado,
        isGroups ? grupoNormalizado : null
      );
      setName("");
      setGroupName("");
      setMensaje("Equipo agregado al torneo correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardando(false);
    }
  }

  async function manejarCargaMasiva(evento) {
    evento.preventDefault();

    if (!bulkContent.trim()) {
      setError("La carga masiva no puede estar vacia.");
      return;
    }

    setGuardandoMasivo(true);
    setMensaje("");
    setError("");

    try {
      await agregarEquiposTorneoMasivo(tournamentId, bulkContent.trim());
      setBulkContent("");
      setMensaje("Equipos agregados al torneo correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardandoMasivo(false);
    }
  }

  async function manejarActualizarGrupo(tournamentTeamId) {
    const nuevoGrupo = (groupEdits[tournamentTeamId] || "").trim();

    if (!nuevoGrupo) {
      setError("El grupo es obligatorio.");
      return;
    }

    setActualizandoGrupoId(tournamentTeamId);
    setMensaje("");
    setError("");

    try {
      await actualizarGrupoEquipoTorneo(
        tournamentId,
        tournamentTeamId,
        nuevoGrupo
      );
      setMensaje("Grupo actualizado correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setActualizandoGrupoId(null);
    }
  }

  async function confirmarEliminar(tournamentTeamId) {
    setEliminandoId(tournamentTeamId);
    setMensaje("");
    setError("");

    try {
      await eliminarEquipoTorneo(tournamentId, tournamentTeamId);
      setConfirmandoId(null);
      setMensaje("Equipo quitado del torneo correctamente.");
      await cargarDatos();
    } catch (error) {
      setError(error.message);
    } finally {
      setEliminandoId(null);
    }
  }

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-tournament-teams-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="primary">Equipos</Badge>
            <h1>{torneo?.name || "Equipos del torneo"}</h1>
            <p>Administra las participaciones segun el formato del torneo.</p>
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
            <span>Cargando equipos...</span>
          </div>
        )}

        {!cargando && torneo && (
          <>
            <Card className="admin-tournament-detail-card">
              <Badge size="sm" variant={isGroups ? "amber" : "primary"}>
                {formatoLabel[torneo.format || "LEAGUE"]}
              </Badge>
              <h2>{torneo.name}</h2>
              <p>
                {isGroups
                  ? "Este torneo organiza equipos separados por grupo."
                  : "Este torneo usa una unica tabla general."}
              </p>
            </Card>

            <Card className="admin-tournament-form-card">
              <form className="admin-tournament-form" onSubmit={manejarAgregarEquipo}>
                <div className="admin-field">
                  <label htmlFor="tournamentTeamName">Nombre</label>
                  <input
                    disabled={guardando}
                    id="tournamentTeamName"
                    maxLength={100}
                    name="tournamentTeamName"
                    onChange={(evento) => setName(evento.target.value)}
                    required
                    type="text"
                    value={name}
                  />
                </div>

                {isGroups && (
                  <div className="admin-field">
                    <label htmlFor="tournamentTeamGroup">Grupo</label>
                    <input
                      disabled={guardando}
                      id="tournamentTeamGroup"
                      maxLength={20}
                      name="tournamentTeamGroup"
                      onChange={(evento) => setGroupName(evento.target.value)}
                      required
                      type="text"
                      value={groupName}
                    />
                  </div>
                )}

                <Button isLoading={guardando} type="submit">
                  {guardando ? "Agregando..." : "Agregar equipo"}
                </Button>
              </form>
            </Card>

            <Card className="admin-tournament-form-card">
              <form className="admin-tournament-form" onSubmit={manejarCargaMasiva}>
                <div className="admin-field">
                  <label htmlFor="tournamentTeamsBulk">Carga masiva</label>
                  <textarea
                    disabled={guardandoMasivo}
                    id="tournamentTeamsBulk"
                    name="tournamentTeamsBulk"
                    onChange={(evento) => setBulkContent(evento.target.value)}
                    placeholder={
                      isGroups
                        ? "Argentina|J\nArgelia|J\nAustria|J"
                        : "River Plate\nBoca Juniors\nRacing Club"
                    }
                    rows={6}
                    value={bulkContent}
                  />
                </div>

                <Button isLoading={guardandoMasivo} type="submit" variant="secondary">
                  {guardandoMasivo ? "Cargando..." : "Cargar equipos"}
                </Button>
              </form>

              {mensaje && (
                <p className="admin-success" role="status">
                  {mensaje}
                </p>
              )}

              <ErrorMessage message={error} />
            </Card>

            {equipos.length === 0 && (
              <EmptyState
                description="Cuando agregues equipos, van a aparecer en esta pantalla."
                title="No hay equipos en este torneo"
              />
            )}

            {equipos.length > 0 && (
              <section className="admin-tournament-team-groups">
                {equiposAgrupados.map((grupo) => (
                  <Card className="admin-tournament-team-group" key={grupo.groupName}>
                    <h2>{grupo.groupName}</h2>

                    <div className="admin-tournament-team-list">
                      {grupo.teams.map((equipo) => (
                        <div className="admin-tournament-team-row" key={equipo.id}>
                          <div>
                            <Badge size="sm" variant="neutral">
                              ID {equipo.teamId}
                            </Badge>
                            <strong>{equipo.teamName}</strong>
                          </div>

                          {isGroups && (
                            <div className="admin-tournament-team-row__group">
                              <div className="admin-field">
                                <label htmlFor={`group-${equipo.id}`}>Grupo</label>
                                <input
                                  disabled={actualizandoGrupoId === equipo.id}
                                  id={`group-${equipo.id}`}
                                  maxLength={20}
                                  onChange={(evento) =>
                                    setGroupEdits((actual) => ({
                                      ...actual,
                                      [equipo.id]: evento.target.value,
                                    }))
                                  }
                                  value={groupEdits[equipo.id] || ""}
                                />
                              </div>
                              <Button
                                disabled={actualizandoGrupoId !== null}
                                isLoading={actualizandoGrupoId === equipo.id}
                                onClick={() => manejarActualizarGrupo(equipo.id)}
                                type="button"
                                variant="secondary"
                              >
                                {actualizandoGrupoId === equipo.id
                                  ? "Actualizando..."
                                  : "Cambiar grupo"}
                              </Button>
                            </div>
                          )}

                          {confirmandoId === equipo.id ? (
                            <div className="admin-team-confirm">
                              <p>Quitar este equipo del torneo?</p>
                              <div className="admin-team-confirm__actions">
                                <Button
                                  disabled={eliminandoId === equipo.id}
                                  onClick={() => setConfirmandoId(null)}
                                  type="button"
                                  variant="secondary"
                                >
                                  Cancelar
                                </Button>
                                <Button
                                  isLoading={eliminandoId === equipo.id}
                                  onClick={() => confirmarEliminar(equipo.id)}
                                  type="button"
                                  variant="danger"
                                >
                                  {eliminandoId === equipo.id
                                    ? "Quitando..."
                                    : "Confirmar"}
                                </Button>
                              </div>
                            </div>
                          ) : (
                            <Button
                              disabled={eliminandoId !== null}
                              onClick={() => setConfirmandoId(equipo.id)}
                              type="button"
                              variant="danger"
                            >
                              Quitar
                            </Button>
                          )}
                        </div>
                      ))}
                    </div>
                  </Card>
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

export default AdminTournamentTeamsPage;
