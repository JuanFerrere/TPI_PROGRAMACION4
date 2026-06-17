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

export async function obtenerTorneosDisponibles() {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/available`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar los torneos.");
  }

  return leerRespuesta(respuesta);
}

export async function obtenerTorneo(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo cargar el torneo.");
  }

  return leerRespuesta(respuesta);
}

export async function obtenerPartidos(tournamentId, matchDayId) {
  const token = obtenerToken();
  const url = new URL(`${TOURNAMENTS_URL}/${tournamentId}/matches`);

  if (matchDayId) {
    url.searchParams.set("matchDayId", matchDayId);
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

export async function obtenerMisPronosticos(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/predictions/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar tus pronosticos.");
  }

  return leerRespuesta(respuesta);
}

export async function guardarPronostico(tournamentId, matchId, homeGoals, awayGoals) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/predictions/matches/${matchId}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        predictedHomeGoals: homeGoals,
        predictedAwayGoals: awayGoals,
      }),
    }
  );

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo guardar el pronostico.");
  }

  return leerRespuesta(respuesta);
}

export async function obtenerRanking(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/rankings/global`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo cargar el ranking.");
  }

  return leerRespuesta(respuesta);
}
