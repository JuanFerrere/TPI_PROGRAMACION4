import { useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import { registrarUsuario } from "../services/authService";
import "../App.css";

function Registro() {
  const navigate = useNavigate();

  const [formulario, setFormulario] = useState({
    username: "",
    email: "",
    password: "",
  });

  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  function manejarCambio(evento) {
    const { name, value } = evento.target;

    setFormulario({
      ...formulario,
      [name]: value,
    });
  }

  async function manejarSubmit(evento) {
    evento.preventDefault();

    setMensaje("");
    setError("");
    setCargando(true);

    try {
      await registrarUsuario(formulario);

      setMensaje("Usuario registrado correctamente.");

      setFormulario({
        username: "",
        email: "",
        password: "",
      });

      navigate("/login");
    } catch (error) {
      setError(error.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="auth-page">
      <Card className="auth-card" padding="lg">
        <div className="auth-header">
          <Badge variant="amber">Nueva cuenta</Badge>
          <h1>Crear cuenta</h1>
          <p>Sumate al prode, armá tus pronósticos y empezá a competir.</p>
        </div>

        <form className="auth-form" onSubmit={manejarSubmit}>
          <div className="auth-field">
            <label htmlFor="username">Nombre de usuario</label>
            <input
              autoComplete="username"
              id="username"
              name="username"
              onChange={manejarCambio}
              required
              type="text"
              value={formulario.username}
            />
          </div>

          <div className="auth-field">
            <label htmlFor="email">Email</label>
            <input
              autoComplete="email"
              id="email"
              name="email"
              onChange={manejarCambio}
              required
              type="email"
              value={formulario.email}
            />
          </div>

          <div className="auth-field">
            <label htmlFor="password">Contraseña</label>
            <input
              autoComplete="new-password"
              id="password"
              minLength={8}
              name="password"
              onChange={manejarCambio}
              required
              type="password"
              value={formulario.password}
            />
          </div>

          <ErrorMessage message={error} />

          {mensaje && (
            <p className="auth-success" role="status">
              {mensaje}
            </p>
          )}

          <Button fullWidth isLoading={cargando} type="submit">
            {cargando ? "Registrando..." : "Crear cuenta"}
          </Button>
        </form>

        <div className="auth-footer">
          <p>¿Ya tenés cuenta?</p>
          <Button onClick={() => navigate("/login")} variant="ghost">
            Iniciar sesión
          </Button>
        </div>

        <Button
          className="auth-back-button"
          onClick={() => navigate("/")}
          variant="secondary"
        >
          Volver al inicio
        </Button>
      </Card>
    </main>
  );
}

export default Registro;
