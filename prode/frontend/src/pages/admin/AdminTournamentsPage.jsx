import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import {
  actualizarFormatoTorneo,
  actualizarEstadoTorneo,
  crearTorneo,
  obtenerTorneos,
} from "../../services/tournamentService.js";
import "../../App.css";

const estados = [
  { value: "ACTIVE", label: "Activo", variant: "success" },
  { value: "FINISHED", label: "Finalizado", variant: "primary" },
];

const formatos = [
  {
    value: "GROUPS",
    label: "Por grupos",
    description: "Los equipos se organizan en grupos independientes.",
  },
  {
    value: "LEAGUE",
    label: "Tabla general",
    description: "Todos los equipos compiten en una única tabla.",
  },
];

const estadoPorValor = estados.reduce((acc, estado) => {
  acc[estado.value] = estado;
  return acc;
}, {});

const formatoPorValor = formatos.reduce((acc, formato) => {
  acc[formato.value] = formato;
  return acc;
}, {});

function leerSesion() {
  return {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
  };
}

function estadoEditable(status) {
  return status === "FINISHED" ? "FINISHED" : "ACTIVE";
}

function formatoEditable(format) {
  return format === "GROUPS" ? "GROUPS" : "LEAGUE";
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

function AdminTournamentsPage() {
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneos, setTorneos] = useState([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [format, setFormat] = useState("LEAGUE");
  const [statusPorTorneo, setStatusPorTorneo] = useState({});
  const [formatPorTorneo, setFormatPorTorneo] = useState({});
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [actualizandoId, setActualizandoId] = useState(null);
  const [actualizandoFormatoId, setActualizandoFormatoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const resumen = useMemo(
    () =>
      torneos.reduce(
        (acc, torneo) => {
          acc.total += 1;

          if (torneo.status === "ACTIVE") {
            acc.activos += 1;
          }

          if (torneo.status === "FINISHED") {
            acc.finalizados += 1;
          }

          return acc;
        },
        { total: 0, activos: 0, finalizados: 0 }
      ),
    [torneos]
  );

  const formatoSeleccionado = formatoPorValor[format] || formatoPorValor.LEAGUE;

  const cargarTorneos = useCallback(async (mostrarCarga = true) => {
    if (mostrarCarga) {
      setCargando(true);
    }

    setError("");

    try {
      const data = await obtenerTorneos();
      const lista = Array.isArray(data) ? data : [];
      setTorneos(lista);
      setStatusPorTorneo(
        lista.reduce((acc, torneo) => {
          acc[torneo.id] = estadoEditable(torneo.status);
          return acc;
        }, {})
      );
      setFormatPorTorneo(
        lista.reduce((acc, torneo) => {
          acc[torneo.id] = formatoEditable(torneo.format);
          return acc;
        }, {})
      );
    } catch (error) {
      setError(error.message);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    if (!sesion.token) {
      navigate("/login", { replace: true });
      return;
    }

    if (sesion.role !== "ADMIN") {
      navigate("/dashboard", { replace: true });
      return;
    }

    Promise.resolve().then(() => cargarTorneos());
  }, [cargarTorneos, navigate, sesion.role, sesion.token]);

  async function manejarCrearTorneo(evento) {
    evento.preventDefault();

    const nombreNormalizado = name.trim();
    const descripcionNormalizada = description.trim();

    if (!nombreNormalizado) {
      setError("El nombre del torneo es obligatorio.");
      return;
    }

    setGuardando(true);
    setMensaje("");
    setError("");

    try {
      await crearTorneo(nombreNormalizado, descripcionNormalizada, format);
      setName("");
      setDescription("");
      setFormat("LEAGUE");
      setMensaje("Torneo creado correctamente.");
      await cargarTorneos(false);
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardando(false);
    }
  }

  async function manejarActualizarEstado(tournamentId) {
    const status = statusPorTorneo[tournamentId];

    if (!status) {
      setError("Seleccioná un estado para el torneo.");
      return;
    }

    setActualizandoId(tournamentId);
    setMensaje("");
    setError("");

    try {
      await actualizarEstadoTorneo(tournamentId, status);
      setMensaje("Estado del torneo actualizado correctamente.");
      await cargarTorneos(false);
    } catch (error) {
      setError(error.message);
    } finally {
      setActualizandoId(null);
    }
  }

  async function manejarActualizarFormato(tournamentId) {
    const nuevoFormato = formatPorTorneo[tournamentId];

    if (!nuevoFormato) {
      setError("Seleccioná un formato para el torneo.");
      return;
    }

    setActualizandoFormatoId(tournamentId);
    setMensaje("");
    setError("");

    try {
      await actualizarFormatoTorneo(tournamentId, nuevoFormato);
      setMensaje("Formato del torneo actualizado correctamente.");
      await cargarTorneos(false);
    } catch (error) {
      setError(error.message);
    } finally {
      setActualizandoFormatoId(null);
    }
  }

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-tournaments-page">
      <section className="admin-shell">
        <header className="admin-header admin-tournaments-header">
          <div>
            <Badge variant="amber">TORNEOS</Badge>
            <h1>Gestión de torneos</h1>
            <p>
              Creá competencias y administrá toda su configuración desde un
              único lugar.
            </p>
          </div>

          <div className="admin-header__actions">
            <Button onClick={() => navigate("/admin")} variant="secondary">
              Volver al panel admin
            </Button>
          </div>
        </header>

        <section className="admin-tournament-summary" aria-label="Resumen de torneos">
          <Card className="admin-tournament-summary-card">
            <span>Total de torneos</span>
            <strong>{resumen.total}</strong>
          </Card>
          <Card className="admin-tournament-summary-card">
            <span>Activos</span>
            <strong>{resumen.activos}</strong>
          </Card>
          <Card className="admin-tournament-summary-card">
            <span>Finalizados</span>
            <strong>{resumen.finalizados}</strong>
          </Card>
        </section>

        <Card className="admin-tournament-form-card admin-tournament-form-card--featured">
          <div className="admin-tournament-form-card__header">
            <div>
              <Badge size="sm" variant="amber">
                NUEVA COMPETENCIA
              </Badge>
              <h2>Crear nuevo torneo</h2>
              <p>Definí los datos principales de la competencia.</p>
            </div>
          </div>

          <form
            className="admin-tournament-form admin-tournament-form--featured"
            onSubmit={manejarCrearTorneo}
          >
            <div className="admin-field">
              <label htmlFor="tournamentName">Nombre</label>
              <input
                disabled={guardando}
                id="tournamentName"
                maxLength={120}
                name="tournamentName"
                onChange={(evento) => setName(evento.target.value)}
                required
                type="text"
                value={name}
              />
            </div>

            <div className="admin-field">
              <label htmlFor="tournamentDescription">Descripción opcional</label>
              <textarea
                disabled={guardando}
                id="tournamentDescription"
                maxLength={500}
                name="tournamentDescription"
                onChange={(evento) => setDescription(evento.target.value)}
                rows={4}
                value={description}
              />
            </div>

            <div className="admin-field admin-field--format">
              <label htmlFor="tournamentFormat">Formato</label>
              <select
                disabled={guardando}
                id="tournamentFormat"
                name="tournamentFormat"
                onChange={(evento) => setFormat(evento.target.value)}
                value={format}
              >
                {formatos.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
              <p className="admin-field__hint">{formatoSeleccionado.description}</p>
            </div>

            <div className="admin-tournament-form__submit">
              <Button isLoading={guardando} type="submit">
                {guardando ? "Creando torneo..." : "Crear torneo"}
              </Button>
            </div>
          </form>

          {mensaje && (
            <p className="admin-success" role="status">
              {mensaje}
            </p>
          )}

          <ErrorMessage message={error} />
        </Card>

        {cargando && (
          <div className="admin-loading" role="status">
            <Spinner size={28} />
            <span>Cargando torneos...</span>
          </div>
        )}

        {!cargando && torneos.length === 0 && (
          <EmptyState
            description="Cuando crees torneos, van a aparecer en este listado."
            title="No hay torneos disponibles"
          />
        )}

        {!cargando && torneos.length > 0 && (
          <section
            aria-label="Listado de torneos"
            className="admin-tournaments-list"
          >
            <div className="admin-tournaments-list__header">
              <h2>Torneos creados</h2>
              <p>
                Seleccioná una competencia para modificarla o administrar su
                contenido.
              </p>
            </div>

            {torneos.map((torneo) => {
              const estadoActual = estadoEditable(torneo.status);
              const formatoActual = formatoEditable(torneo.format);
              const estado = estadoPorValor[estadoActual] || estadoPorValor.ACTIVE;
              const formato = formatoPorValor[formatoActual] || formatoPorValor.LEAGUE;
              const actualizandoEstado = actualizandoId === torneo.id;
              const actualizandoFormato = actualizandoFormatoId === torneo.id;
              const controlesDeshabilitados =
                actualizandoEstado || actualizandoFormato;

              return (
                <Card className="admin-tournament-card" key={torneo.id}>
                  <div className="admin-tournament-card__info">
                    <div className="admin-tournament-card__badges">
                      <Badge size="sm" variant={estado.variant}>
                        {estado.label}
                      </Badge>
                      <Badge size="sm" variant="primary">
                        {formato.label}
                      </Badge>
                    </div>
                    <h2>{torneo.name || "Torneo sin nombre"}</h2>
                    <p>
                      {torneo.description ||
                        "Este torneo todavía no tiene descripción."}
                    </p>
                    <span>Creado: {formatearFecha(torneo.createdAt)}</span>
                  </div>

                  <div className="admin-tournament-card__actions">
                    <span className="admin-tournament-card__actions-title">
                      Configuración rápida
                    </span>

                    <div className="admin-tournament-card__control">
                      <div className="admin-field">
                        <label htmlFor={`tournamentStatus-${torneo.id}`}>
                          Estado
                        </label>
                        <select
                          disabled={controlesDeshabilitados}
                          id={`tournamentStatus-${torneo.id}`}
                          onChange={(evento) =>
                            setStatusPorTorneo((actual) => ({
                              ...actual,
                              [torneo.id]: evento.target.value,
                            }))
                          }
                          value={statusPorTorneo[torneo.id] || estadoActual}
                        >
                          {estados.map((item) => (
                            <option key={item.value} value={item.value}>
                              {item.label}
                            </option>
                          ))}
                        </select>
                      </div>

                      <Button
                        disabled={controlesDeshabilitados}
                        isLoading={actualizandoEstado}
                        onClick={() => manejarActualizarEstado(torneo.id)}
                        type="button"
                        variant="secondary"
                      >
                        {actualizandoEstado
                          ? "Actualizando..."
                          : "Actualizar estado"}
                      </Button>
                    </div>

                    <div className="admin-tournament-card__control">
                      <div className="admin-field">
                        <label htmlFor={`tournamentFormat-${torneo.id}`}>
                          Formato
                        </label>
                        <select
                          disabled={controlesDeshabilitados}
                          id={`tournamentFormat-${torneo.id}`}
                          onChange={(evento) =>
                            setFormatPorTorneo((actual) => ({
                              ...actual,
                              [torneo.id]: evento.target.value,
                            }))
                          }
                          value={formatPorTorneo[torneo.id] || formatoActual}
                        >
                          {formatos.map((item) => (
                            <option key={item.value} value={item.value}>
                              {item.label}
                            </option>
                          ))}
                        </select>
                      </div>

                      <Button
                        disabled={controlesDeshabilitados}
                        isLoading={actualizandoFormato}
                        onClick={() => manejarActualizarFormato(torneo.id)}
                        type="button"
                        variant="secondary"
                      >
                        {actualizandoFormato
                          ? "Actualizando..."
                          : "Actualizar formato"}
                      </Button>
                    </div>

                    <Button
                      disabled={controlesDeshabilitados}
                      onClick={() => navigate(`/admin/tournaments/${torneo.id}`)}
                      type="button"
                    >
                      Administrar torneo
                    </Button>
                  </div>
                </Card>
              );
            })}
          </section>
        )}
      </section>
    </main>
  );
}

export default AdminTournamentsPage;
