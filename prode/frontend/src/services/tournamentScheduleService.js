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

export async function obtenerFechasDelTorneo(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/match-days`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar las fechas.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function crearFecha(tournamentId, name, orderNumber) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/match-days`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name,
      orderNumber: orderNumber || null,
    }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo crear la fecha.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function crearFechasMasivamente(tournamentId, matchDays) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/match-days/bulk`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ matchDays }),
    }
  );

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron crear las fechas.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function eliminarFecha(tournamentId, matchDayId) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/match-days/${matchDayId}`,
    {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (respuesta.status === 204) {
    return true;
  }

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo eliminar la fecha.");
  }

  return true;
}

export async function obtenerPartidosDelTorneo(tournamentId, matchDayId) {
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

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function crearPartido(tournamentId, data) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/matches`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo crear el partido.");
  }

  const responseData = await leerRespuesta(respuesta);
  return responseData;
}

export async function crearPartidosMasivamente(tournamentId, data) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/matches/bulk`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron crear los partidos.");
  }

  const responseData = await leerRespuesta(respuesta);
  return responseData;
}

export async function eliminarPartido(tournamentId, matchId) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/matches/${matchId}`,
    {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (respuesta.status === 204) {
    return true;
  }

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo eliminar el partido.");
  }

  return true;
}
