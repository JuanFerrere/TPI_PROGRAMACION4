const MATCHES_URL = `${import.meta.env.VITE_API_URL}/api/matches`;

function limpiarSesion() {
  localStorage.removeItem("token");
  localStorage.removeItem("tokenType");
  localStorage.removeItem("username");
  localStorage.removeItem("email");
  localStorage.removeItem("role");
}

async function leerRespuesta(respuesta) {
  return respuesta.json().catch(() => null);
}

export async function obtenerPartidos() {
  const token = localStorage.getItem("token");

  if (!token) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error("Necesitás iniciar sesión para ver partidos.");
  }

  const respuesta = await fetch(MATCHES_URL, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await leerRespuesta(respuesta);

  if (respuesta.status === 401) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error(data?.message || "Sesión expirada. Iniciá sesión nuevamente.");
  }

  if (!respuesta.ok) {
    throw new Error(data?.message || "No se pudieron cargar los partidos.");
  }

  return data;
}
