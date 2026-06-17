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

export async function obtenerTorneos() {
  const token = obtenerToken();

  const respuesta = await fetch(TOURNAMENTS_URL, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar los torneos.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function obtenerTorneoPorId(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo cargar el torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function crearTorneo(name, description, format) {
  const token = obtenerToken();

  const respuesta = await fetch(TOURNAMENTS_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name,
      description: description || null,
      format,
    }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo crear el torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function actualizarFormatoTorneo(tournamentId, format) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/format`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ format }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo actualizar el formato del torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function actualizarEstadoTorneo(tournamentId, status) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/status`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ status }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo actualizar el estado del torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function obtenerEquiposTorneo(tournamentId) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/teams`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar los equipos del torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function agregarEquipoTorneo(tournamentId, name, groupName) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/teams`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name,
      groupName: groupName || null,
    }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo agregar el equipo al torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function agregarEquiposTorneoMasivo(tournamentId, content) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TOURNAMENTS_URL}/${tournamentId}/teams/bulk`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ content }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron agregar los equipos al torneo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function actualizarGrupoEquipoTorneo(
  tournamentId,
  tournamentTeamId,
  groupName
) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/teams/${tournamentTeamId}/group`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ groupName }),
    }
  );

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo actualizar el grupo del equipo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function eliminarEquipoTorneo(tournamentId, tournamentTeamId) {
  const token = obtenerToken();

  const respuesta = await fetch(
    `${TOURNAMENTS_URL}/${tournamentId}/teams/${tournamentTeamId}`,
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
    await manejarError(respuesta, "No se pudo quitar el equipo del torneo.");
  }

  return true;
}
