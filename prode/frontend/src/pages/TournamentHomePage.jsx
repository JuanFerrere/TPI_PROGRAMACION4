import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import Spinner from "../components/ui/Spinner.jsx";
import { obtenerTorneo } from "../services/userTournamentService.js";
import "../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const formatoVariant = {
  GROUPS: "primary",
  LEAGUE: "amber",
};

const estadoLabel = {
  ACTIVE: "Activo",
  FINISHED: "Finalizado",
};

const estadoVariant = {
  ACTIVE: "success",
  FINISHED: "primary",
};

const descripcionDefault =
  "Consultá los partidos, realizá tus pronósticos y seguí toda la competencia.";

function TournamentFlow({ estado }) {
  const pasos =
    estado === "FINISHED"
      ? ["Partidos", "Resultados", "Tabla final", "Eliminatorias"]
      : ["Partidos", "Pronósticos", "Resultados", "Ranking"];

  return (
    <div className="tournament-home-flow" aria-label="Recorrido del torneo">
      <span className="tournament-home-flow__title">Recorrido del torneo</span>
      <div className="tournament-home-flow__steps">
        {pasos.map((paso, index) => (
          <div className="tournament-home-flow__item" key={paso}>
            <span className="tournament-home-flow__step">{paso}</span>
            {index < pasos.length - 1 && (
              <span className="tournament-home-flow__arrow" aria-hidden="true">
                →
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

function ActionJourney({ steps }) {
  return (
    <div className="tournament-home-action-card__journey" aria-label="Recorrido">
      {steps.map((step, index) => (
        <span key={step}>
          {step}
          {index < steps.length - 1 && <strong aria-hidden="true">→</strong>}
        </span>
      ))}
    </div>
  );
}

function TournamentActionCard({
  accent = "primary",
  badge,
  buttonLabel,
  buttonVariant = "secondary",
  description,
  featured = false,
  index,
  journey,
  onClick,
  title,
}) {
  return (
    <Card
      className={`tournament-home-action-card tournament-home-action-card--${accent}${
        featured ? " tournament-home-action-card--featured" : ""
      }`}
    >
      <div className="tournament-home-action-card__top">
        <span className="tournament-home-action-card__number">{index}</span>
        <Badge size="sm" variant={buttonVariant === "amber" ? "amber" : "primary"}>
          {badge}
        </Badge>
      </div>
      <div className="tournament-home-action-card__body">
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
      {journey && <ActionJourney steps={journey} />}
      <Badge className="tournament-home-action-card__availability" size="sm">
        Disponible
      </Badge>
      <Button fullWidth onClick={onClick} variant={buttonVariant}>
        {buttonLabel}
      </Button>
    </Card>
  );
}

function TournamentHomePage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [torneo, setTorneo] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  const formato = torneo?.format || "LEAGUE";
  const estado = torneo?.status || "ACTIVE";
  const descripcionTorneo = torneo?.description || descripcionDefault;
  const torneoFinalizado = estado === "FINISHED";
  const textoEstado = torneoFinalizado
    ? "El torneo finalizó. Podés consultar los resultados, tus puntos y la clasificación final."
    : "El torneo está activo. Ya podés consultar los partidos y cargar o actualizar tus pronósticos.";
  const descripcionTabla =
    formato === "GROUPS"
      ? "Consultá las posiciones, puntos, goles y diferencia de cada grupo."
      : "Consultá la tabla general, los puntos, goles y diferencia de gol.";

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    async function cargarTorneo() {
      setCargando(true);
      setError("");

      try {
        setTorneo(await obtenerTorneo(tournamentId));
      } catch (error) {
        setError(error.message);
      } finally {
        setCargando(false);
      }
    }

    cargarTorneo();
  }, [navigate, tournamentId]);

  return (
    <main className="tournament-page tournament-home-page">
      <section className="tournament-shell tournament-home-shell">
        <header className="tournament-header tournament-home-header">
          <div>
            <Badge variant="primary">Torneo</Badge>
            <h1>{torneo?.name || "Torneo"}</h1>
            <p>{descripcionTorneo}</p>
          </div>

          <Button onClick={() => navigate("/tournaments")} variant="secondary">
            Volver a torneos
          </Button>
        </header>

        {cargando && (
          <div className="matches-loading tournament-home-loading" role="status">
            <Spinner size={28} />
            <span>Cargando torneo...</span>
          </div>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && torneo && (
          <>
            <Card className="tournament-home-hero">
              <div className="tournament-home-hero__content">
                <div className="tournament-home-hero__badges">
                  <Badge size="sm" variant={estadoVariant[estado] || "neutral"}>
                    {estadoLabel[estado] || estado}
                  </Badge>
                  <Badge size="sm" variant={formatoVariant[formato] || "neutral"}>
                    {formatoLabel[formato] || "Tabla general"}
                  </Badge>
                </div>
                <h2>{torneo.name}</h2>
                <p>{descripcionTorneo}</p>
                <div className="tournament-home-hero__status">
                  <span>Estado de la competencia</span>
                  <strong>{textoEstado}</strong>
                </div>
              </div>

              <TournamentFlow estado={estado} />
            </Card>

            <section className="tournament-home-actions">
              <TournamentActionCard
                accent="fixture"
                badge="FIXTURE"
                buttonLabel="Ver partidos"
                buttonVariant="primary"
                description="Consultá los encuentros del torneo y cargá tus marcadores antes del cierre."
                featured
                index="01"
                journey={["Elegir partido", "Cargar marcador", "Esperar resultado"]}
                onClick={() => navigate(`/tournaments/${tournamentId}/matches`)}
                title="Partidos y pronósticos"
              />

              <div className="tournament-home-actions__pair">
                <TournamentActionCard
                  accent="plays"
                  badge="MIS JUGADAS"
                  buttonLabel="Ver pronósticos"
                  buttonVariant="amber"
                  description="Revisá tus marcadores, resultados y puntos obtenidos en este torneo."
                  index="02"
                  onClick={() => navigate(`/tournaments/${tournamentId}/predictions`)}
                  title="Mis pronósticos"
                />

                <TournamentActionCard
                  accent="ranking"
                  badge="CLASIFICACIÓN"
                  buttonLabel="Ver ranking"
                  buttonVariant="success"
                  description="Compará tus puntos con los demás participantes de esta competencia."
                  index="03"
                  onClick={() => navigate(`/tournaments/${tournamentId}/ranking`)}
                  title="Ranking"
                />
              </div>

              <div className="tournament-home-actions__pair">
                <TournamentActionCard
                  accent="standings"
                  badge="POSICIONES"
                  buttonLabel="Ver tabla"
                  buttonVariant="secondary"
                  description={descripcionTabla}
                  index="04"
                  onClick={() => navigate(`/tournaments/${tournamentId}/standings`)}
                  title="Tabla deportiva"
                />

                <TournamentActionCard
                  accent="knockout"
                  badge="FASE FINAL"
                  buttonLabel="Ver llave"
                  buttonVariant="secondary"
                  description="Consultá los cruces, resultados y equipos clasificados en cada ronda."
                  index="05"
                  onClick={() => navigate(`/tournaments/${tournamentId}/knockout`)}
                  title="Llave eliminatoria"
                />
              </div>
            </section>
          </>
        )}
      </section>
    </main>
  );
}

export default TournamentHomePage;
