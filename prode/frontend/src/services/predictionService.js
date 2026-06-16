const PREDICTIONS_URL = `${import.meta.env.VITE_API_URL}/api/predictions/matches`;
const MY_PREDICTIONS_URL = `${import.meta.env.VITE_API_URL}/api/predictions/me`;

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

export async function guardarPronostico(
  matchId,
  predictedHomeGoals,
  predictedAwayGoals
) {
  const token = localStorage.getItem("token");

  if (!token) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error("Necesitás iniciar sesión para guardar pronósticos.");
  }

  const respuesta = await fetch(`${PREDICTIONS_URL}/${matchId}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      predictedHomeGoals,
      predictedAwayGoals,
    }),
  });

  const data = await leerRespuesta(respuesta);

  if (respuesta.status === 401) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error(data?.message || "Sesión expirada. Iniciá sesión nuevamente.");
  }

  if (!respuesta.ok) {
    throw new Error(data?.message || "No se pudo guardar el pronóstico.");
  }

  return data;
}

export async function obtenerMisPronosticos(matchStatus) {
  const token = localStorage.getItem("token");

  if (!token) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error("Necesitás iniciar sesión para ver tus pronósticos.");
  }

  const url = new URL(MY_PREDICTIONS_URL);

  if (matchStatus) {
    url.searchParams.set("matchStatus", matchStatus);
  }

  const respuesta = await fetch(url.toString(), {
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
    throw new Error(data?.message || "No se pudieron cargar tus pronósticos.");
  }

  return data;
}
