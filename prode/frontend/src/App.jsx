import { useNavigate } from "react-router";

function App() {
  const navigate = useNavigate();

  function irARegistro() {
    navigate("/registro");
  }

  function irALogin() {
    navigate("/login");
  }

  return (
    <main>
      <h1>Prode</h1>

      <p>Bienvenido al sistema de pronósticos deportivos.</p>

      <button onClick={irARegistro}>Registrarse</button>
      <button onClick={irALogin}>Iniciar sesión</button>
    </main>
  );
}

export default App;