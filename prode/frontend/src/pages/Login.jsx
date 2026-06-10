import { useState } from "react";
import { useNavigate } from "react-router";
import { iniciarSesion } from "../services/authService";

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

    } catch (error) {
      setError(error.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <main>
      <h1>Iniciar sesión</h1>

      <form onSubmit={manejarSubmit}>
        <div>
          <label>Usuario o email</label>
          <br />
          <input
            type="text"
            name="usernameOrEmail"
            value={formulario.usernameOrEmail}
            onChange={manejarCambio}
            required
          />
        </div>

        <br />

        <div>
          <label>Contraseña</label>
          <br />
          <input
            type="password"
            name="password"
            value={formulario.password}
            onChange={manejarCambio}
            required
          />
        </div>

        <br />

        <button type="submit" disabled={cargando}>
          {cargando ? "Ingresando..." : "Iniciar sesión"}
        </button>
      </form>

      {mensaje && <p>{mensaje}</p>}
      {error && <p>{error}</p>}

      <br />

      <button onClick={() => navigate("/")}>Volver al inicio</button>
    </main>
  );
}

export default Login;
