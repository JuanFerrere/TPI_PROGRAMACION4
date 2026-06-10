import { useState } from "react";
import { useNavigate } from "react-router";
import { registrarUsuario } from "../services/authService";

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
    <main>
      <h1>Registro</h1>

      <form onSubmit={manejarSubmit}>
        <div>
          <label>Nombre de usuario</label>
          <br />
          <input
            type="text"
            name="username"
            value={formulario.username}
            onChange={manejarCambio}
            required
          />
        </div>

        <br />

        <div>
          <label>Email</label>
          <br />
          <input
            type="email"
            name="email"
            value={formulario.email}
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
            minLength={8}
          />
        </div>

        <br />

        <button type="submit" disabled={cargando}>
          {cargando ? "Registrando..." : "Registrarse"}
        </button>
      </form>

      {mensaje && <p>{mensaje}</p>}
      {error && <p>{error}</p>}

      <br />

      <button onClick={() => navigate("/")}>Volver al inicio</button>
    </main>
  );
}

export default Registro;