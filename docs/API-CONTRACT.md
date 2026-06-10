# Contrato API — Prode Backend

Documento de referencia para el equipo de frontend (React). Describe todos los
endpoints disponibles, sus cuerpos de petición, respuestas, roles requeridos y
errores posibles.

- **Base URL (desarrollo):** `http://localhost:8080`
- **Formato:** JSON en todos los request y response.
- **Autenticación:** JWT vía header `Authorization: Bearer <token>` (excepto los
  endpoints públicos marcados más abajo). El backend es **stateless**: no usa
  cookies ni sesión de servidor.
- **Zonas horarias:** todas las fechas son `Instant` en **UTC ISO-8601**, por
  ejemplo `2026-06-20T19:00:00Z`.
- **CORS:** habilitado solo para `http://localhost:5173` (Vite).

> Sugerencia para el front: centralizar la base URL en `VITE_API_URL` y usar un
> interceptor de axios/fetch que agregue el header `Authorization` y maneje el
> `401` (redirigir a login) de forma global.

---

## Convención de códigos HTTP

Todos los errores devuelven el **mismo formato** (ver al final). El front debe
ramificar por `status`:

| Código | Significado | Qué debería hacer el front |
|--------|-------------|----------------------------|
| `200 OK` | Operación exitosa | Renderizar datos |
| `201 Created` | Recurso creado | Idem, con feedback de creación |
| `204 No Content` | Éxito sin cuerpo (delete / salir de grupo) | No parsear body |
| `400 Bad Request` | Validación o **regla de negocio** incumplida | Mostrar `message` al usuario (toast) |
| `401 Unauthorized` | Falta token, vencido o credenciales inválidas | Redirigir a login |
| `403 Forbidden` | Autenticado pero **sin rol** (acción solo ADMIN) | Mostrar "no autorizado" |
| `404 Not Found` | El recurso no existe | Mostrar "no encontrado" |
| `500 Internal Server Error` | Error inesperado | Mostrar error genérico |

> ⚠️ **Importante para el front:** algunas violaciones de permiso de **grupos
> privados** (no sos miembro, el owner no puede salir, ya pertenecés) se devuelven
> como **`400`**, no como `403`. El `403` queda reservado para "te falta el rol
> ADMIN". Ramificar leyendo siempre `message`.

---

## Roles

- **Público** (sin token): registro, login y health.
- **Autenticado** (`USER` o `ADMIN`): consultas y acciones de jugador.
- **ADMIN**: alta/baja/edición de equipos, fechas, partidos, inicio y carga de
  resultados.

El `role` del usuario llega en el login y en `GET /api/auth/me`, para que el
front decida si muestra el panel de administración.

---

## 1. Auth

| Método | Endpoint | Rol | Body | Response | Errores |
|--------|----------|-----|------|----------|---------|
| POST | `/api/auth/register` | Público | `RegisterRequest` | `201` `AuthResponse` | `400` validación / username o email duplicado |
| POST | `/api/auth/login` | Público | `LoginRequest` | `200` `AuthResponse` | `400` validación · `401` credenciales inválidas |
| GET | `/api/auth/me` | Autenticado | — | `200` `UserResponse` | `401` sin token |

**RegisterRequest**
```json
{ "username": "juan", "email": "juan@test.com", "password": "Password123!" }
```
- `username`: obligatorio.
- `email`: obligatorio, formato válido.
- `password`: obligatorio, mínimo 8 caracteres.

**LoginRequest**
```json
{ "usernameOrEmail": "juan", "password": "Password123!" }
```

**AuthResponse**
```json
{ "token": "eyJhbGci...", "tokenType": "Bearer", "username": "juan", "email": "juan@test.com", "role": "USER" }
```

---

## 2. Health

| Método | Endpoint | Rol | Response |
|--------|----------|-----|----------|
| GET | `/api/health` | Público | `200` estado del servicio |

---

## 3. Teams (equipos)

| Método | Endpoint | Rol | Body | Response | Errores |
|--------|----------|-----|------|----------|---------|
| GET | `/api/teams` | Autenticado | — (query `?name=` opcional) | `200` `TeamResponse[]` | `401` |
| GET | `/api/teams/{id}` | Autenticado | — | `200` `TeamResponse` | `401` · `404` |
| POST | `/api/teams` | ADMIN | `{ "name": "Boca Juniors" }` | `201` `TeamResponse` | `400` nombre duplicado/ vacío · `403` |
| DELETE | `/api/teams/{id}` | ADMIN | — | `204` | `400` asociado a un partido · `403` · `404` |

**TeamResponse**
```json
{ "id": 1, "name": "Boca Juniors", "createdAt": "2026-06-09T18:00:00Z" }
```

---

## 4. MatchDays (fechas / jornadas)

| Método | Endpoint | Rol | Body | Response | Errores |
|--------|----------|-----|------|----------|---------|
| GET | `/api/match-days` | Autenticado | — (query `?status=` opcional) | `200` `MatchDayResponse[]` | `401` · `400` status inválido |
| GET | `/api/match-days/{id}` | Autenticado | — | `200` `MatchDayResponse` | `401` · `404` |
| POST | `/api/match-days` | ADMIN | `{ "name": "Fecha 1" }` | `201` `MatchDayResponse` | `400` · `403` |
| PUT | `/api/match-days/{id}` | ADMIN | `{ "name": "Fecha 1 - Grupos" }` | `200` `MatchDayResponse` | `400` no PROGRAMADA / con partidos · `403` · `404` |
| DELETE | `/api/match-days/{id}` | ADMIN | — | `204` | `400` no PROGRAMADA / con partidos · `403` · `404` |

`status` puede ser `PROGRAMADA`, `EN_JUEGO` o `FINALIZADA` (lo calcula el backend).

**MatchDayResponse**
```json
{ "id": 1, "name": "Fecha 1", "status": "PROGRAMADA", "createdAt": "2026-06-09T18:00:00Z" }
```

---

## 5. Matches (partidos)

| Método | Endpoint | Rol | Body | Response | Errores |
|--------|----------|-----|------|----------|---------|
| GET | `/api/matches` | Autenticado | — (query `?matchDayId=` opcional) | `200` `MatchResponse[]` (orden por `startTime` asc) | `401` |
| GET | `/api/matches/{id}` | Autenticado | — | `200` `MatchResponse` | `401` · `404` |
| POST | `/api/matches` | ADMIN | `MatchCreateRequest` | `201` `MatchResponse` | `400` local = visitante / ids inexistentes · `403` |
| PUT | `/api/matches/{id}` | ADMIN | `MatchUpdateRequest` | `200` `MatchResponse` | `400` no POR_JUGARSE / con pronósticos · `403` · `404` |
| PATCH | `/api/matches/{id}/start` | ADMIN | — | `200` `MatchResponse` (pasa a `EN_JUEGO`) | `400` no POR_JUGARSE · `403` · `404` |
| PATCH | `/api/matches/{id}/result` | ADMIN | `MatchResultRequest` | `200` `MatchResponse` (pasa a `FINALIZADO`) | `400` no EN_JUEGO / goles negativos · `403` · `404` |
| DELETE | `/api/matches/{id}` | ADMIN | — | `204` | `400` no POR_JUGARSE / con pronósticos · `403` · `404` |

**MatchCreateRequest**
```json
{ "matchDayId": 1, "homeTeamId": 1, "awayTeamId": 2, "startTime": "2026-06-20T19:00:00Z" }
```
**MatchUpdateRequest** (sin `matchDayId`)
```json
{ "homeTeamId": 1, "awayTeamId": 2, "startTime": "2026-06-21T19:00:00Z" }
```
**MatchResultRequest** (solo goles; tendencia y puntos los calcula el backend)
```json
{ "homeGoals": 2, "awayGoals": 1 }
```

**MatchResponse** (sirve directo para las cards de partido)
```json
{
  "id": 1,
  "matchDayId": 1,
  "matchDayName": "Fecha 1",
  "homeTeamId": 1,
  "homeTeamName": "Boca Juniors",
  "awayTeamId": 2,
  "awayTeamName": "River Plate",
  "startTime": "2026-06-20T19:00:00Z",
  "status": "POR_JUGARSE",
  "homeGoals": null,
  "awayGoals": null,
  "resultTrend": null,
  "createdAt": "2026-06-09T18:00:00Z",
  "updatedAt": "2026-06-09T18:00:00Z"
}
```
- `status`: `POR_JUGARSE` | `EN_JUEGO` | `FINALIZADO`.
- `homeGoals` / `awayGoals` / `resultTrend`: `null` hasta que el partido finaliza.
- `resultTrend`: `LOCAL` | `EMPATE` | `VISITANTE`.

---

## 6. Predictions (pronósticos)

Todos requieren autenticación. El pronóstico siempre pertenece al usuario del token.

| Método | Endpoint | Rol | Body | Response | Errores |
|--------|----------|-----|------|----------|---------|
| POST | `/api/predictions/matches/{matchId}` | Autenticado | `PredictionUpsertRequest` | `200` `PredictionResponse` (crea o actualiza) | `400` partido no POR_JUGARSE / ventana cerrada / goles negativos · `401` · `404` |
| GET | `/api/predictions/me` | Autenticado | — (query `?matchStatus=` opcional) | `200` `PredictionResponse[]` | `401` |
| GET | `/api/predictions/matches/{matchId}` | Autenticado | — | `200` `PredictionResponse[]` | `400` período de carga aún abierto · `401` · `404` |

- **Upsert:** un solo pronóstico por usuario+partido. Reenviar el POST actualiza el existente.
- **Ventana de carga:** se bloquea desde **30 minutos antes** del `startTime`.
- **Privacidad:** los pronósticos de un partido (`GET .../matches/{matchId}`) solo
  se ven una vez cerrada la ventana de carga; antes devuelve `400`.

**PredictionUpsertRequest**
```json
{ "predictedHomeGoals": 2, "predictedAwayGoals": 1 }
```

**PredictionResponse**
```json
{
  "id": 10,
  "userId": 2,
  "username": "juan",
  "matchId": 1,
  "matchDayName": "Fecha 1",
  "homeTeamName": "Boca Juniors",
  "awayTeamName": "River Plate",
  "startTime": "2026-06-20T19:00:00Z",
  "status": "POR_JUGARSE",
  "predictedHomeGoals": 2,
  "predictedAwayGoals": 1,
  "predictedTrend": "LOCAL",
  "points": 0,
  "exactHit": false,
  "createdAt": "2026-06-09T18:10:00Z",
  "updatedAt": "2026-06-09T18:10:00Z"
}
```
- `points`: `0` hasta que el partido finaliza (luego `3`, `1` o `0`).
- `exactHit`: `true` solo cuando acierta el marcador exacto (3 puntos).

---

## 7. Rankings

| Método | Endpoint | Rol | Response | Notas |
|--------|----------|-----|----------|-------|
| GET | `/api/rankings/global` | Autenticado | `200` `RankingResponse[]` | Lista vacía si no hay puntos (no es error) |

**RankingResponse**
```json
{ "position": 1, "userId": 2, "username": "juan", "totalPoints": 9, "exactHits": 2, "predictionsCount": 5 }
```
Orden: más puntos → más aciertos exactos → `username` alfabético.

---

## 8. Groups (grupos privados)

Todos requieren autenticación. La validación de pertenencia la hace el backend.

| Método | Endpoint | Rol | Body | Response | Errores |
|--------|----------|-----|------|----------|---------|
| POST | `/api/groups` | Autenticado | `{ "name": "Amigos UTN" }` | `201` `GroupResponse` | `400` nombre vacío / > 100 · `401` |
| POST | `/api/groups/join` | Autenticado | `{ "inviteCode": "3F2504E0" }` | `200` `GroupResponse` | `400` código inexistente / ya es miembro · `401` |
| GET | `/api/groups/me` | Autenticado | — | `200` `GroupResponse[]` | `401` |
| GET | `/api/groups/{groupId}` | Miembro | — | `200` `GroupDetailResponse` | `400` no es miembro · `401` · `404` |
| DELETE | `/api/groups/{groupId}/members/me` | Miembro | — | `204` | `400` owner no puede salir / no es miembro · `401` · `404` |
| GET | `/api/groups/{groupId}/ranking` | Miembro | — | `200` `RankingResponse[]` | `400` no es miembro · `401` · `404` |

**GroupResponse**
```json
{ "id": 1, "name": "Amigos UTN", "inviteCode": "3F2504E0", "ownerId": 2, "ownerUsername": "juan", "membersCount": 1, "createdAt": "2026-06-09T18:00:00Z" }
```

**GroupDetailResponse** (incluye miembros)
```json
{
  "id": 1,
  "name": "Amigos UTN",
  "inviteCode": "3F2504E0",
  "ownerId": 2,
  "ownerUsername": "juan",
  "membersCount": 2,
  "members": [
    { "userId": 2, "username": "juan",  "joinedAt": "2026-06-09T18:00:00Z" },
    { "userId": 3, "username": "benja", "joinedAt": "2026-06-09T18:05:00Z" }
  ],
  "createdAt": "2026-06-09T18:00:00Z"
}
```

El ranking de grupo usa el mismo `RankingResponse` que el global, pero solo con
los miembros del grupo (todos aparecen, aunque tengan 0 puntos).

---

## Formato de error (todos los endpoints)

```json
{
  "timestamp": "2026-06-09T18:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "No se puede modificar un partido que ya comenzo",
  "path": "/api/matches/1"
}
```

El campo `message` está pensado para mostrarse directamente al usuario.

---

## Flujo típico para el front

1. `POST /api/auth/login` → guardar `token` y `role`.
2. Si `role === "ADMIN"`: habilitar panel de gestión (teams, fechas, partidos, resultados).
3. `GET /api/matches` → cards de partidos.
4. `POST /api/predictions/matches/{id}` → cargar pronóstico (si POR_JUGARSE y fuera de la ventana de 30 min).
5. `GET /api/predictions/me` → "mis pronósticos" con puntos.
6. `GET /api/rankings/global` → tabla global.
7. `POST /api/groups` / `POST /api/groups/join` → grupos privados.
8. `GET /api/groups/{id}/ranking` → tabla del grupo.
