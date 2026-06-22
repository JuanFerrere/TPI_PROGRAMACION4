import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import { obtenerTorneoPorId } from "../../services/tournamentService.js";
import "../../App.css";

const secciones = [
  {
    title: "Equipos y grupos",
    description: "Administra los equipos asociados a este torneo.",
    path: "teams",
    enabled: true,
  },
  {
    title: "Fechas y partidos",
    description: "Organiza las jornadas y carga los encuentros del torneo.",
    path: "schedule",
    enabled: true,
  },
  {
    title: "Resultados",
    description: "Carga los marcadores finales y actualiza los puntos del Prode.",
    path: "results",
    enabled: true,
  },
  {
    title: "Eliminatorias",
    description: "Genera la llave y avanza rondas.",
    path: "knockout",
    enabled: true,
  },
];

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

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

function AdminTournamentDetailPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneo, setTorneo] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let activo = true;

    async function cargarTorneo() {
      if (!sesion.token) {
        navigate("/login", { replace: true });
        return;
      }

      if (sesion.role !== "ADMIN") {
        navigate("/dashboard", { replace: true });
        return;
      }

      setCargando(true);
      setError("");

      try {
        const data = await obtenerTorneoPorId(tournamentId);

        if (activo) {
          setTorneo(data);
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

    cargarTorneo();

    return () => {
      activo = false;
    };
  }, [navigate, sesion.role, sesion.token, tournamentId]);

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-tournament-detail-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="amber">Torneo</Badge>
            <h1>{torneo?.name || "Detalle de torneo"}</h1>
            <p>Administra equipos, fechas, partidos y resultados del torneo.</p>
          </div>

          <div className="admin-header__actions">
            <Button
              onClick={() => navigate("/admin/tournaments")}
              variant="secondary"
            >
              Volver a torneos
            </Button>
          </div>
        </header>

        {cargando && (
          <div className="admin-loading" role="status">
            <Spinner size={28} />
            <span>Cargando torneo...</span>
          </div>
        )}

        {!cargando && error && <ErrorMessage message={error} />}

        {!cargando && !error && torneo && (
          <>
            <Card className="admin-tournament-detail-card">
              <Badge
                size="sm"
                variant={estadoVariant[torneo.status] || "neutral"}
              >
                {torneo.status}
              </Badge>
              <Badge size="sm" variant="primary">
                {formatoLabel[torneo.format || "LEAGUE"]}
              </Badge>
              <h2>{torneo.name}</h2>
              <p>
                {torneo.description ||
                  "Este torneo todavia no tiene descripcion."}
              </p>
              <span>Creado: {formatearFecha(torneo.createdAt)}</span>
            </Card>

            <section
              aria-label="Secciones del torneo"
              className="admin-tournament-sections"
            >
              {secciones.map((seccion) => (
                <Card className="admin-tournament-section-card" key={seccion.title}>
                  <Badge size="sm" variant="neutral">
                    {seccion.enabled ? "Disponible" : "Proximamente"}
                  </Badge>
                  <h2>{seccion.title}</h2>
                  <p>{seccion.description}</p>
                  <Button
                    disabled={!seccion.enabled}
                    fullWidth
                    onClick={() =>
                      seccion.enabled &&
                      navigate(`/admin/tournaments/${tournamentId}/${seccion.path}`)
                    }
                    variant="secondary"
                  >
                    {seccion.path === "knockout" ? "Administrar llave" : "Gestionar"}
                  </Button>
                </Card>
              ))}
            </section>
          </>
        )}
      </section>
    </main>
  );
}

export default AdminTournamentDetailPage;
