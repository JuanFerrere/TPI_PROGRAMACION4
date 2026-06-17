import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import {
  actualizarEstadoTorneo,
  crearTorneo,
  obtenerTorneos,
} from "../../services/tournamentService.js";
import "../../App.css";

const estados = ["DRAFT", "ACTIVE", "FINISHED", "ARCHIVED"];

const estadoVariant = {
  DRAFT: "neutral",
  ACTIVE: "success",
  FINISHED: "primary",
  ARCHIVED: "amber",
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

function AdminTournamentsPage() {
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneos, setTorneos] = useState([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [statusPorTorneo, setStatusPorTorneo] = useState({});
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [actualizandoId, setActualizandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

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
          acc[torneo.id] = torneo.status;
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
      await crearTorneo(nombreNormalizado, descripcionNormalizada);
      setName("");
      setDescription("");
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
      setError("Selecciona un estado para el torneo.");
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

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-tournaments-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="amber">Torneos</Badge>
            <h1>Gestion de torneos</h1>
            <p>Crea y administra las competencias disponibles en el Prode.</p>
          </div>

          <div className="admin-header__actions">
            <Button onClick={() => navigate("/admin")} variant="secondary">
              Volver al panel admin
            </Button>
          </div>
        </header>

        <Card className="admin-tournament-form-card">
          <form
            className="admin-tournament-form"
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
              <label htmlFor="tournamentDescription">
                Descripcion opcional
              </label>
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

            <Button isLoading={guardando} type="submit">
              {guardando ? "Creando..." : "Crear torneo"}
            </Button>
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
            {torneos.map((torneo) => (
              <Card className="admin-tournament-card" key={torneo.id}>
                <div className="admin-tournament-card__info">
                  <Badge
                    size="sm"
                    variant={estadoVariant[torneo.status] || "neutral"}
                  >
                    {torneo.status}
                  </Badge>
                  <h2>{torneo.name || "Torneo sin nombre"}</h2>
                  <p>
                    {torneo.description ||
                      "Este torneo todavia no tiene descripcion."}
                  </p>
                  <span>Creado: {formatearFecha(torneo.createdAt)}</span>
                </div>

                <div className="admin-tournament-card__actions">
                  <div className="admin-field">
                    <label htmlFor={`tournamentStatus-${torneo.id}`}>
                      Estado
                    </label>
                    <select
                      disabled={actualizandoId !== null}
                      id={`tournamentStatus-${torneo.id}`}
                      onChange={(evento) =>
                        setStatusPorTorneo((actual) => ({
                          ...actual,
                          [torneo.id]: evento.target.value,
                        }))
                      }
                      value={statusPorTorneo[torneo.id] || torneo.status}
                    >
                      {estados.map((estado) => (
                        <option key={estado} value={estado}>
                          {estado}
                        </option>
                      ))}
                    </select>
                  </div>

                  <Button
                    disabled={actualizandoId !== null}
                    isLoading={actualizandoId === torneo.id}
                    onClick={() => manejarActualizarEstado(torneo.id)}
                    type="button"
                    variant="secondary"
                  >
                    {actualizandoId === torneo.id
                      ? "Actualizando..."
                      : "Actualizar estado"}
                  </Button>

                  <Button
                    disabled={actualizandoId !== null}
                    onClick={() => navigate(`/admin/tournaments/${torneo.id}`)}
                    type="button"
                  >
                    Administrar torneo
                  </Button>
                </div>
              </Card>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default AdminTournamentsPage;
