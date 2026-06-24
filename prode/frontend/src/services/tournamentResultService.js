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

export async function obtenerPartidosParaResultados(tournamentId, filtros = {}) {
  const token = obtenerToken();
  const url = new URL(`${TOURNAMENTS_URL}/${tournamentId}/matches`);

  if (filtros.matchDayId) {
    url.searchParams.set("matchDayId", filtros.matchDayId);
  }

  if (filtros.status) {
    url.searchParams.set("status", filtros.status);
  }

  const respuesta = await fetch(url.toString(), {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar los partidos.");
  }

  return leerRespuesta(respuesta);
}

export async function guardarResultado(
  tournamentId,
  matchId,
  homeGoals,
  awayGoals,
  homePenaltyGoals = null,
  awayPenaltyGoals = null
) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/matches/${matchId}/result`,
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        homeGoals,
        awayGoals,
        homePenaltyGoals,
        awayPenaltyGoals,
      }),
    }
  );

  if (!respuesta.ok) {
    await manejarError(
      respuesta,
      "No se pudo guardar el resultado."
    );
  }

  return leerRespuesta(respuesta);
}
