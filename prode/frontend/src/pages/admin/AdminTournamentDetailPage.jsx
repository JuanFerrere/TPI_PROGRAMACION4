import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import { obtenerTorneoPorId } from "../../services/tournamentService.js";
import "../../App.css";

const seccionesBase = [
  {
    number: "01",
    label: "Participantes",
    title: "Equipos y grupos",
    leagueTitle: "Equipos",
    description:
      "Administrá los equipos asociados y organizalos según el formato del torneo.",
    leagueDescription:
      "Administrá los equipos asociados a la tabla general del torneo.",
    actionLabel: "Gestionar equipos",
    path: "teams",
    enabled: true,
    accent: "success",
  },
  {
    number: "02",
    label: "Calendario",
    title: "Fechas y partidos",
    description: "Organizá las jornadas, cruces y horarios de la competencia.",
    actionLabel: "Gestionar calendario",
    path: "schedule",
    enabled: true,
    accent: "primary",
  },
  {
    number: "03",
    label: "Marcadores",
    title: "Resultados",
    description: "Cargá los resultados finales y actualizá los puntos del Prode.",
    actionLabel: "Gestionar resultados",
    path: "results",
    enabled: true,
    accent: "amber",
  },
  {
    number: "04",
    label: "Fase final",
    title: "Eliminatorias",
    description: "Generá la llave y administrá el avance de las rondas finales.",
    actionLabel: "Administrar llave",
    path: "knockout",
    enabled: true,
    accent: "danger",
  },
];

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const estadoLabel = {
  DRAFT: "Borrador",
  ACTIVE: "Activo",
  FINISHED: "Finalizado",
  ARCHIVED: "Archivado",
};

const estadoVariant = {
  DRAFT: "neutral",
  ACTIVE: "success",
  FINISHED: "primary",
  ARCHIVED: "amber",
};

const flujoAdministrativo = [
  "Equipos",
  "Fechas y partidos",
  "Resultados",
  "Eliminatorias",
];

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

function obtenerSecciones(format) {
  const esLiga = format === "LEAGUE";

  return seccionesBase.map((seccion) => {
    if (seccion.path !== "teams" || !esLiga) {
      return seccion;
    }

    return {
      ...seccion,
      title: seccion.leagueTitle,
      description: seccion.leagueDescription,
    };
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
            <p>
              Administrá los equipos, fechas, partidos y resultados de esta
              competencia.
            </p>
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
            <Card className="admin-tournament-detail-card admin-tournament-detail-card--hero">
              <div className="admin-tournament-detail-card__content">
                <div className="admin-tournament-detail-card__badges">
                  <Badge
                    size="sm"
                    variant={estadoVariant[torneo.status] || "neutral"}
                  >
                    {estadoLabel[torneo.status] || "Estado no disponible"}
                  </Badge>
                  <Badge size="sm" variant="primary">
                    {formatoLabel[torneo.format || "LEAGUE"]}
                  </Badge>
                </div>
                <h2>{torneo.name}</h2>
                <p>
                  {torneo.description ||
                    "Este torneo todavía no tiene descripción."}
                </p>
                <span className="admin-tournament-detail-card__date">
                  Creado: {formatearFecha(torneo.createdAt)}
                </span>
              </div>

              <div
                className="admin-tournament-flow"
                aria-label="Flujo administrativo del torneo"
              >
                <span className="admin-tournament-flow__title">
                  Flujo operativo
                </span>
                <div className="admin-tournament-flow__steps">
                  {flujoAdministrativo.map((paso, index) => (
                    <div className="admin-tournament-flow__item" key={paso}>
                      <span className="admin-tournament-flow__step">{paso}</span>
                      {index < flujoAdministrativo.length - 1 && (
                        <span
                          className="admin-tournament-flow__arrow"
                          aria-hidden="true"
                        >
                          →
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </Card>

            <section
              aria-label="Secciones del torneo"
              className="admin-tournament-sections"
            >
              {obtenerSecciones(torneo.format || "LEAGUE").map((seccion) => (
                <Card
                  className={`admin-tournament-section-card admin-tournament-section-card--${seccion.accent}`}
                  key={seccion.path}
                >
                  <div className="admin-tournament-section-card__top">
                    <span className="admin-tournament-section-card__number">
                      {seccion.number}
                    </span>
                    <Badge size="sm" variant={seccion.enabled ? "success" : "neutral"}>
                      {seccion.enabled ? "Disponible" : "No disponible"}
                    </Badge>
                  </div>
                  <span className="admin-tournament-section-card__label">
                    {seccion.label}
                  </span>
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
                    {seccion.actionLabel}
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
