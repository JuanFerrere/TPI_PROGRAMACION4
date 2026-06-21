const API_URL = `${import.meta.env.VITE_API_URL}/api`;

export async function getGlobalRanking() {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_URL}/rankings/global`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || "No se pudo cargar el ranking");
  }

  return response.json();
}
