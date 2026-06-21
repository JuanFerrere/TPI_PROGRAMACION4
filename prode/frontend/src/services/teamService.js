const TEAMS_URL = `${import.meta.env.VITE_API_URL}/api/teams`;

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
    throw new Error("Necesitás iniciar sesión.");
  }

  return token;
}

async function manejarError(respuesta, mensajeDefault) {
  const data = await leerRespuesta(respuesta);

  if (respuesta.status === 401) {
    limpiarSesion();
    window.location.replace("/login");
    throw new Error(data?.message || "Sesión expirada. Iniciá sesión nuevamente.");
  }

  throw new Error(data?.message || mensajeDefault);
}

export async function obtenerEquipos(name) {
  const token = obtenerToken();
  const url = new URL(TEAMS_URL);

  if (name) {
    url.searchParams.set("name", name);
  }

  const respuesta = await fetch(url.toString(), {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudieron cargar los equipos.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function crearEquipo(name) {
  const token = obtenerToken();

  const respuesta = await fetch(TEAMS_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ name }),
  });

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo crear el equipo.");
  }

  const data = await leerRespuesta(respuesta);
  return data;
}

export async function eliminarEquipo(id) {
  const token = obtenerToken();

  const respuesta = await fetch(`${TEAMS_URL}/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (respuesta.status === 204) {
    return true;
  }

  if (!respuesta.ok) {
    await manejarError(respuesta, "No se pudo eliminar el equipo.");
  }

  return true;
}
