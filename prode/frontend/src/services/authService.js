const API_URL = "http://localhost:8080/api/auth";

async function enviarRequest(url, datos) {
  const respuesta = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(datos),
  });

  const data = await respuesta.json().catch(() => null);

  if (!respuesta.ok) {
    throw new Error(data?.message || "Ocurrió un error en la petición");
  }

  return data;
}

export function registrarUsuario(usuario) {
  return enviarRequest(`${API_URL}/register`, usuario);
}

export function iniciarSesion(credenciales) {
  return enviarRequest(`${API_URL}/login`, credenciales);
}
