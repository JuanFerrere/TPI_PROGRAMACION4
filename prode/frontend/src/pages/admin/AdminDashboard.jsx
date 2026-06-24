import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import "../../App.css";

const adminJourney = [
  "Torneo",
  "Equipos y grupos",
  "Fechas y partidos",
  "Resultados",
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
            <p>Gestioná los torneos y todo su contenido desde un único lugar.</p>
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

        <Card className="admin-welcome-card admin-welcome-card--compact">
          <div className="admin-welcome-card__marker" aria-hidden="true">
            AD
          </div>
          <div>
            <span>Administrador</span>
            <strong>{sesion.username || "Admin"}</strong>
            <p>Acceso habilitado para configurar la operatoria del Prode UTN.</p>
          </div>
        </Card>

        <section
          className="admin-grid admin-grid--single"
          aria-label="Herramientas administrativas"
        >
          <Card className="admin-primary-card">
            <div className="admin-primary-card__content">
              <Badge size="sm" variant="amber">
                CENTRO DE GESTIÓN
              </Badge>
              <h2>Torneos</h2>
              <p>
                Creá competencias y administrá desde cada torneo sus equipos,
                fechas, partidos y resultados.
              </p>
              <div className="admin-primary-card__actions">
                <Button onClick={() => navigate("/admin/tournaments")}>
                  Gestionar torneos
                </Button>
              </div>
            </div>

            <div
              className="admin-journey"
              aria-label="Recorrido administrativo por torneo"
            >
              <span className="admin-journey__title">Flujo de gestión</span>
              <div className="admin-journey__steps">
                {adminJourney.map((step, index) => (
                  <div className="admin-journey__item" key={step}>
                    <div className="admin-journey-step">
                      <span className="admin-journey-step__index">
                        {index + 1}
                      </span>
                      <span>{step}</span>
                    </div>
                    {index < adminJourney.length - 1 && (
                      <span className="admin-journey__arrow" aria-hidden="true">
                        →
                      </span>
                    )}
                  </div>
                ))}
              </div>
              <div className="admin-fixture-lines" aria-hidden="true">
                <span />
                <span />
                <span />
              </div>
            </div>
          </Card>
        </section>
      </section>
    </main>
  );
}

export default AdminDashboard;
