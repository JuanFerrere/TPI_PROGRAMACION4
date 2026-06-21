import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import {
  crearEquipo,
  eliminarEquipo,
  obtenerEquipos,
} from "../../services/teamService.js";
import "../../App.css";

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

function AdminTeamsPage() {
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [equipos, setEquipos] = useState([]);
  const [nuevoNombre, setNuevoNombre] = useState("");
  const [busqueda, setBusqueda] = useState("");
  const [filtro, setFiltro] = useState("");
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [eliminandoId, setEliminandoId] = useState(null);
  const [confirmandoId, setConfirmandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const cargarEquipos = useCallback(async (name, mostrarCarga = true) => {
    if (mostrarCarga) {
      setCargando(true);
    }

    setError("");

    try {
      const data = await obtenerEquipos(name || undefined);
      setEquipos(Array.isArray(data) ? data : []);
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

    Promise.resolve().then(() => cargarEquipos(filtro));
  }, [cargarEquipos, filtro, navigate, sesion.role, sesion.token]);

  async function manejarCrearEquipo(evento) {
    evento.preventDefault();

    const name = nuevoNombre.trim();

    if (!name) {
      setError("El nombre del equipo es obligatorio.");
      return;
    }

    setGuardando(true);
    setMensaje("");
    setError("");

    try {
      await crearEquipo(name);
      setNuevoNombre("");
      setMensaje("Equipo creado correctamente.");
      await cargarEquipos(filtro, false);
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardando(false);
    }
  }

  function manejarBuscar(evento) {
    evento.preventDefault();
    setMensaje("");
    setConfirmandoId(null);
    setFiltro(busqueda.trim());
  }

  function limpiarBusqueda() {
    setBusqueda("");
    setFiltro("");
    setMensaje("");
    setConfirmandoId(null);
  }

  async function confirmarEliminacion(id) {
    setEliminandoId(id);
    setMensaje("");
    setError("");

    try {
      await eliminarEquipo(id);
      setMensaje("Equipo eliminado correctamente.");
      setConfirmandoId(null);
      await cargarEquipos(filtro, false);
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
    <main className="admin-page admin-teams-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="primary">Equipos</Badge>
            <h1>Gestión de equipos</h1>
            <p>Creá y administrá los equipos disponibles para los partidos.</p>
          </div>

          <div className="admin-header__actions">
            <Button onClick={() => navigate("/admin")} variant="secondary">
              Volver al panel admin
            </Button>
          </div>
        </header>

        <Card className="admin-team-form-card">
          <form className="admin-team-form" onSubmit={manejarCrearEquipo}>
            <div className="admin-team-field">
              <label htmlFor="teamName">Nombre del equipo</label>
              <input
                disabled={guardando}
                id="teamName"
                maxLength={100}
                name="teamName"
                onChange={(evento) => setNuevoNombre(evento.target.value)}
                required
                type="text"
                value={nuevoNombre}
              />
            </div>

            <Button isLoading={guardando} type="submit">
              {guardando ? "Creando..." : "Crear equipo"}
            </Button>
          </form>

          {mensaje && (
            <p className="admin-team-success" role="status">
              {mensaje}
            </p>
          )}

          <ErrorMessage message={error} />
        </Card>

        <Card className="admin-team-filter-card">
          <form className="admin-team-filter" onSubmit={manejarBuscar}>
            <div className="admin-team-field">
              <label htmlFor="teamSearch">Buscar equipo</label>
              <input
                id="teamSearch"
                name="teamSearch"
                onChange={(evento) => setBusqueda(evento.target.value)}
                type="text"
                value={busqueda}
              />
            </div>

            <div className="admin-team-filter__actions">
              <Button type="submit" variant="secondary">
                Buscar
              </Button>
              <Button onClick={limpiarBusqueda} type="button" variant="ghost">
                Limpiar
              </Button>
            </div>
          </form>
        </Card>

        {cargando && (
          <div className="admin-teams-loading" role="status">
            <Spinner size={28} />
            <span>Cargando equipos...</span>
          </div>
        )}

        {!cargando && equipos.length === 0 && (
          <EmptyState
            description="Cuando crees equipos, van a aparecer en este listado."
            title="No hay equipos disponibles"
          />
        )}

        {!cargando && equipos.length > 0 && (
          <section className="admin-teams-list" aria-label="Listado de equipos">
            {equipos.map((equipo) => (
              <Card className="admin-team-card" key={equipo.id}>
                <div className="admin-team-card__info">
                  <Badge size="sm" variant="neutral">
                    ID {equipo.id}
                  </Badge>
                  <h2>{equipo.name || "Equipo sin nombre"}</h2>
                  <p>Creado: {formatearFecha(equipo.createdAt)}</p>
                </div>

                {confirmandoId === equipo.id ? (
                  <div className="admin-team-confirm">
                    <p>¿Seguro que querés eliminar este equipo?</p>
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
                        onClick={() => confirmarEliminacion(equipo.id)}
                        type="button"
                        variant="danger"
                      >
                        {eliminandoId === equipo.id
                          ? "Eliminando..."
                          : "Confirmar eliminación"}
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
                    Eliminar
                  </Button>
                )}
              </Card>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default AdminTeamsPage;
