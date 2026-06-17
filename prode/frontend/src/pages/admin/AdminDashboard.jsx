import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import "../../App.css";

const adminCards = [
  {
    title: "Equipos",
    description: "Crear, listar y eliminar equipos.",
    badge: "Clubes",
    variant: "primary",
  },
  {
    title: "Fechas",
    description: "Organizar las fechas del Prode.",
    badge: "Calendario",
    variant: "amber",
  },
  {
    title: "Partidos",
    description: "Crear partidos y definir horarios.",
    badge: "Fixture",
    variant: "success",
  },
  {
    title: "Resultados",
    description: "Iniciar partidos y cargar resultados.",
    badge: "Carga final",
    variant: "danger",
  },
];

function leerSesion() {
  return {
    token: localStorage.getItem("token"),
    username: localStorage.getItem("username"),
    role: localStorage.getItem("role"),
  };
}

function limpiarSesion() {
  localStorage.removeItem("token");
  localStorage.removeItem("tokenType");
  localStorage.removeItem("username");
  localStorage.removeItem("email");
  localStorage.removeItem("role");
}

function AdminDashboard() {
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);

  useEffect(() => {
    if (!sesion.token) {
      navigate("/login", { replace: true });
      return;
    }

    if (sesion.role !== "ADMIN") {
      navigate("/dashboard", { replace: true });
    }
  }, [navigate, sesion.role, sesion.token]);

  function cerrarSesion() {
    limpiarSesion();
    navigate("/login", { replace: true });
  }

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="amber">ADMIN</Badge>
            <h1>Panel de administración</h1>
            <p>Gestioná equipos, fechas, partidos y resultados.</p>
          </div>

          <div className="admin-header__actions">
            <Button onClick={() => navigate("/dashboard")} variant="secondary">
              Volver al dashboard
            </Button>
            <Button onClick={cerrarSesion} variant="danger">
              Cerrar sesión
            </Button>
          </div>
        </header>

        <Card className="admin-welcome-card">
          <span>Administrador</span>
          <strong>{sesion.username || "Admin"}</strong>
          <p>Acceso habilitado para configurar la operatoria del Prode UTN.</p>
        </Card>

        <section className="admin-grid" aria-label="Herramientas administrativas">
          {adminCards.map((card) => (
            <Card className="admin-action-card" key={card.title}>
              <Badge size="sm" variant={card.variant}>
                {card.badge}
              </Badge>
              <h2>{card.title}</h2>
              <p>{card.description}</p>
              <Button disabled fullWidth variant="secondary">
                Gestionar
              </Button>
            </Card>
          ))}
        </section>
      </section>
    </main>
  );
}

export default AdminDashboard;
