import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import "../App.css";

const dashboardCards = [
  {
    title: "Próximos partidos",
    badge: "Fixture",
    description: "Consultá los partidos disponibles para cargar pronósticos.",
    variant: "primary",
    path: "/matches",
    actionLabel: "Ver partidos",
  },
  {
    title: "Mis pronósticos",
    badge: "Pendientes",
    description: "Revisá tus predicciones y prepará la próxima fecha.",
    variant: "amber",
    path: "/predictions",
    actionLabel: "Ver pronósticos",
  },
  {
    title: "Ranking global",
    badge: "Competencia",
    description: "Seguí tu posición y compará puntos con otros jugadores.",
    variant: "success",
  },
  {
    title: "Grupos privados",
    badge: "Amigos",
    description: "Organizá competencias privadas cuando la función esté lista.",
    variant: "neutral",
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

  return (
    <main className="dashboard-page">
      <section className="dashboard-shell">
        <header className="dashboard-header">
          <div>
            <Badge variant="success">Sesión activa</Badge>
            <h1>Hola, {sesion.username || "Usuario"}</h1>
            <p>Tu panel de Prode UTN ya está listo para las próximas jugadas.</p>
          </div>

          <Button onClick={cerrarSesion} variant="secondary">
            Cerrar sesión
          </Button>
        </header>

        <section className="dashboard-profile" aria-label="Datos de usuario">
          <Card className="dashboard-profile-card">
            <span>Usuario</span>
            <strong>{sesion.username || "Usuario"}</strong>
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

        <section className="dashboard-grid" aria-label="Acciones del dashboard">
          {dashboardCards.map((card) => (
            <Card className="dashboard-action-card" key={card.title}>
              <Badge size="sm" variant={card.variant}>
                {card.badge}
              </Badge>
              <h2>{card.title}</h2>
              <p>{card.description}</p>
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
      </section>
    </main>
  );
}

export default UserDashboard;
