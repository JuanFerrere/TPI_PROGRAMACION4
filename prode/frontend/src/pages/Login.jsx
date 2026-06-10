import { useState } from "react";
import { useNavigate } from "react-router";
import Badge from "../components/ui/Badge.jsx";
import Button from "../components/ui/Button.jsx";
import Card from "../components/ui/Card.jsx";
import ErrorMessage from "../components/ui/ErrorMessage.jsx";
import { iniciarSesion } from "../services/authService";
import "../App.css";

function Login() {
  const navigate = useNavigate();

  const [formulario, setFormulario] = useState({
    usernameOrEmail: "",
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
      const data = await iniciarSesion(formulario);

      localStorage.setItem("token", data.token);
      localStorage.setItem("tokenType", data.tokenType);
      localStorage.setItem("username", data.username);
      localStorage.setItem("email", data.email);
      localStorage.setItem("role", data.role);

      setMensaje("Inicio de sesión correcto.");
      navigate("/dashboard", { replace: true });
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
          <Badge variant="primary">Prode UTN</Badge>
          <h1>Ingresar a Prode UTN</h1>
          <p>Entrá con tu usuario o email para cargar pronósticos y competir.</p>
        </div>

        <form className="auth-form" onSubmit={manejarSubmit}>
          <div className="auth-field">
            <label htmlFor="usernameOrEmail">Usuario o email</label>
            <input
              autoComplete="username"
              id="usernameOrEmail"
              name="usernameOrEmail"
              onChange={manejarCambio}
              required
              type="text"
              value={formulario.usernameOrEmail}
            />
          </div>

          <div className="auth-field">
            <label htmlFor="password">Contraseña</label>
            <input
              autoComplete="current-password"
              id="password"
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
            {cargando ? "Ingresando..." : "Iniciar sesión"}
          </Button>
        </form>

        <div className="auth-footer">
          <p>¿Todavía no tenés cuenta?</p>
          <Button onClick={() => navigate("/registro")} variant="ghost">
            Crear cuenta
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

export default Login;
