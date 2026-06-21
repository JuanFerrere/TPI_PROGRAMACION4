const TOURNAMENTS_URL = `${import.meta.env.VITE_API_URL}/api/tournaments`;

function limpiarSesion() {
  localStorage.removeItem("token");
  localStorage.removeItem("tokenType");
  localStorage.removeItem("username");
  localStorage.removeItem("email");
  localStorage.removeItem("role");
}

async function leerRespuesta(respuesta) {
  if (respuesta.status === 204) {
    return null;
  }

  return respuesta.json().catch(() => null);
}

function obtenerToken() {
  const token = localStorage.getItem("token");

  if (!token) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error("Necesitas iniciar sesion.");
  }

  return token;
}

async function manejarError(respuesta, mensajeDefault) {
  const data = await leerRespuesta(respuesta);

  if (respuesta.status === 401) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error(data?.message || "Sesion expirada. Inicia sesion nuevamente.");
  }

  throw new Error(data?.message || mensajeDefault);
}

export async function getStandings(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/standings`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo cargar la tabla deportiva.");
  }

  return leerRespuesta(respuesta);
}
