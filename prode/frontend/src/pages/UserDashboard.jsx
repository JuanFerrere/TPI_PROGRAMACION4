import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import "../App.css";

const dashboardCards = [
  {
    title: "Torneos",
    badge: "Competencias",
    description: "Elegi una competencia, realiza tus pronosticos y competi en su ranking.",
    variant: "success",
    path: "/tournaments",
    actionLabel: "Ver torneos",
  },
  {
    title: "Proximos partidos",
    badge: "Fixture",
    description: "Consulta los partidos disponibles para cargar pronosticos.",
    variant: "primary",
    path: "/matches",
    actionLabel: "Ver partidos",
  },
  {
    title: "Mis pronosticos",
    badge: "Pendientes",
    description: "Revisa tus predicciones y prepara la proxima fecha.",
    variant: "amber",
    path: "/predictions",
    actionLabel: "Ver pronosticos",
  },
  {
    title: "Ranking global",
    badge: "Competencia",
    description: "Segui tu posicion y compara puntos con otros jugadores.",
    variant: "success",
  },
  {
    title: "Grupos privados",
    badge: "Amigos",
    description: "Organiza competencias privadas cuando la funcion este lista.",
    variant: "neutral",
  },
];

const adminCard = {
  title: "Panel admin",
  badge: "ADMIN",
  description: "Gestiona equipos, fechas, partidos y resultados.",
  variant: "danger",
  path: "/admin",
  actionLabel: "Ir al panel admin",
};

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
  const cards = esAdmin ? [dashboardCards[0], adminCard] : dashboardCards;

  return (
    <main className="dashboard-page">
      <section className="dashboard-shell">
        <header className="dashboard-header">
          <div>
            <Badge variant="success">Sesion activa</Badge>
            <h1>
              {esAdmin
                ? `Panel administrativo, ${sesion.username || "Admin"}`
                : `Hola, ${sesion.username || "Usuario"}`}
            </h1>
            <p>
              {esAdmin
                ? "Gestiona torneos y accede a las herramientas administrativas."
                : "Tu panel de Prode UTN ya esta listo para las proximas jugadas."}
            </p>
          </div>

          <Button onClick={cerrarSesion} variant="secondary">
            Cerrar sesion
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
          {cards.map((card) => (
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
                {card.actionLabel || "Proximamente"}
              </Button>
            </Card>
          ))}
        </section>
      </section>
    </main>
  );
}

export default UserDashboard;
