import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import "../App.css";

const userDashboardCards = [
  {
    title: "Torneos",
    badge: "COMPETENCIAS",
    description:
      "Explorá las competencias disponibles y entrá a cada torneo para realizar tus pronósticos.",
    variant: "success",
    accent: "success",
    path: "/tournaments",
    actionLabel: "Ver torneos",
    journey: [
      "Elegir torneo",
      "Ver partidos",
      "Pronosticar",
      "Revisar jugadas",
    ],
  },
  {
    title: "Próximos partidos",
    badge: "FIXTURE",
    description: "Consultá los encuentros disponibles y prepará tus próximas jugadas.",
    secondary: "Calendario y encuentros",
    variant: "primary",
    accent: "primary",
    path: "/matches",
    actionLabel: "Ver partidos",
  },
  {
    title: "Mis pronósticos",
    badge: "JUGADAS",
    description: "Revisá las predicciones que realizaste y consultá sus resultados.",
    secondary: "Historial personal",
    variant: "amber",
    accent: "amber",
    path: "/predictions",
    actionLabel: "Ver pronósticos",
  },
];

const adminDashboardCards = [
  {
    title: "Torneos",
    badge: "COMPETENCIAS",
    description:
      "Consultá los torneos creados y accedé a la configuración de cada competencia.",
    secondary: "Acceso directo",
    variant: "amber",
    path: "/admin/tournaments",
    actionLabel: "Ver torneos",
    journey: ["Torneos creados", "Seleccionar torneo", "Administrar contenido"],
  },
  {
    title: "Panel administrativo",
    badge: "ADMINISTRACIÓN",
    description:
      "Accedé al centro de gestión para organizar los torneos y toda su configuración.",
    secondary: "Centro de gestión",
    variant: "danger",
    path: "/admin",
    actionLabel: "Ir al panel admin",
    journey: ["Torneos", "Equipos y grupos", "Fechas y partidos", "Resultados"],
  },
];

function leerSesion() {
  return {
    token: localStorage.getItem("token"),
    username: localStorage.getItem("username"),
    email: localStorage.getItem("email"),
    role: localStorage.getItem("role"),
  };
}

function UserDashboard() {
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);

  useEffect(() => {
    if (!sesion.token) {
      navigate("/login", { replace: true });
    }
  }, [navigate, sesion.token]);

  function cerrarSesion() {
    localStorage.removeItem("token");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("username");
    localStorage.removeItem("email");
    localStorage.removeItem("role");

    navigate("/login", { replace: true });
  }

  if (!sesion.token) {
    return null;
  }

  const esAdmin = sesion.role === "ADMIN";

  return (
    <main className="dashboard-page">
      <section className="dashboard-shell">
        <header className="dashboard-header">
          <div>
            <Badge variant={esAdmin ? "amber" : "success"}>
              {esAdmin ? "ADMIN" : "Sesión activa"}
            </Badge>
            <h1>
              {esAdmin
                ? `Panel administrativo, ${sesion.username || "Admin"}`
                : `Hola, ${sesion.username || "Usuario"}`}
            </h1>
            <p>
              {esAdmin
                ? "Administrá las competencias y accedé a las herramientas de gestión."
                : "Elegí un torneo, revisá los próximos partidos y administrá tus pronósticos."}
            </p>
          </div>

          <Button onClick={cerrarSesion} variant="secondary">
            Cerrar sesión
          </Button>
        </header>

        {esAdmin ? (
          <>
            <section className="dashboard-profile" aria-label="Datos de usuario">
              <Card className="dashboard-profile-card">
                <span>Administrador</span>
                <strong>{sesion.username || "Admin"}</strong>
              </Card>
              <Card className="dashboard-profile-card">
                <span>Email</span>
                <strong>{sesion.email || "Sin email"}</strong>
              </Card>
              <Card className="dashboard-profile-card">
                <span>Rol</span>
                <strong>{sesion.role || "Usuario"}</strong>
              </Card>
            </section>

            <section
              className="dashboard-grid dashboard-grid--admin"
              aria-label="Acciones del dashboard"
            >
              {adminDashboardCards.map((card) => (
                <Card
                  className="dashboard-action-card dashboard-action-card--admin"
                  key={card.title}
                >
                  <Badge size="sm" variant={card.variant}>
                    {card.badge}
                  </Badge>
                  <h2>{card.title}</h2>
                  <p>{card.description}</p>
                  {card.secondary && (
                    <span className="dashboard-action-card__secondary">
                      {card.secondary}
                    </span>
                  )}
                  {card.journey && (
                    <div
                      className="dashboard-action-card__journey"
                      aria-label={`Recorrido de ${card.title}`}
                    >
                      {card.journey.map((step, index) => (
                        <span
                          className="dashboard-action-card__journey-step"
                          key={step}
                        >
                          {step}
                          {index < card.journey.length - 1 && (
                            <span aria-hidden="true">→</span>
                          )}
                        </span>
                      ))}
                    </div>
                  )}
                  <Button
                    disabled={!card.path}
                    fullWidth
                    onClick={() => card.path && navigate(card.path)}
                    variant="secondary"
                  >
                    {card.actionLabel || "Próximamente"}
                  </Button>
                </Card>
              ))}
            </section>
          </>
        ) : (
          <>
            <section
              className="dashboard-profile dashboard-profile--user"
              aria-label="Datos de usuario"
            >
              <Card className="dashboard-profile-card dashboard-profile-card--compact">
                <div className="dashboard-profile-card__item">
                  <span>Usuario</span>
                  <strong>{sesion.username || "Usuario"}</strong>
                </div>
                <div className="dashboard-profile-card__item">
                  <span>Email</span>
                  <strong>{sesion.email || "Sin email"}</strong>
                </div>
                <div className="dashboard-profile-card__item">
                  <span>Rol</span>
                  <strong>Usuario</strong>
                </div>
              </Card>
            </section>

            <section
              className="dashboard-user-layout"
              aria-label="Acciones del dashboard"
            >
              <Card className="dashboard-user-hero-card">
                <div className="dashboard-user-hero-card__content">
                  <Badge size="sm" variant={userDashboardCards[0].variant}>
                    {userDashboardCards[0].badge}
                  </Badge>
                  <h2>{userDashboardCards[0].title}</h2>
                  <p>{userDashboardCards[0].description}</p>
                  <div className="dashboard-user-hero-card__actions">
                    <Button onClick={() => navigate(userDashboardCards[0].path)}>
                      {userDashboardCards[0].actionLabel}
                    </Button>
                  </div>
                </div>

                <div
                  className="dashboard-user-flow"
                  aria-label="Recorrido del usuario"
                >
                  <span className="dashboard-user-flow__title">Tu recorrido</span>
                  <div className="dashboard-user-flow__steps">
                    {userDashboardCards[0].journey.map((step, index) => (
                      <div className="dashboard-user-flow__item" key={step}>
                        <span className="dashboard-user-flow__step">{step}</span>
                        {index < userDashboardCards[0].journey.length - 1 && (
                          <span
                            className="dashboard-user-flow__arrow"
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
                className="dashboard-user-secondary-grid"
                aria-label="Acciones secundarias"
              >
                {userDashboardCards.slice(1).map((card) => (
                  <Card
                    className={`dashboard-user-card dashboard-user-card--${card.accent}`}
                    key={card.title}
                  >
                    <Badge size="sm" variant={card.variant}>
                      {card.badge}
                    </Badge>
                    <span className="dashboard-user-card__secondary">
                      {card.secondary}
                    </span>
                    <h2>{card.title}</h2>
                    <p>{card.description}</p>
                    <Button
                      fullWidth
                      onClick={() => navigate(card.path)}
                      variant="secondary"
                    >
                      {card.actionLabel}
                    </Button>
                  </Card>
                ))}
              </section>
            </section>
          </>
        )}
      </section>
    </main>
  );
}

export default UserDashboard;
