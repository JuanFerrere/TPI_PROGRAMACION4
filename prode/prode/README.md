# Prode - Backend

API REST para un sistema Prode full-stack desarrollado como Trabajo Practico
Integrador de Programacion 4, UTN FRVM.

El backend administra torneos deportivos, equipos, fechas, partidos,
pronosticos, ranking de usuarios, tabla deportiva y eliminatorias por torneo.

## Integrantes

- Ferrere, Juan Ignacio.
- Pereyra, Benjamin.
- Rochietti, Geremias.
- Acosta, Mateo.

## Stack

- Java 21.
- Spring Boot 3.4.x.
- Maven Wrapper.
- PostgreSQL.
- Spring Web.
- Spring Data JPA.
- Spring Security.
- JWT con JJWT.
- BCrypt.
- Validation.
- Lombok.
- dotenv-java.

## Requisitos

- JDK 21.
- PostgreSQL iniciado.
- Base de datos `prode_db`.
- Archivo local `.env` en `prode/prode`.
- Maven Wrapper incluido en el proyecto.

## Variables de entorno

El archivo `.env` no debe subirse al repositorio. Guarda credenciales y secretos
locales.

Ejemplo:

```env
DB_URL=jdbc:postgresql://localhost:5432/prode_db
DB_USERNAME=postgres
DB_PASSWORD=TU_PASSWORD

JWT_SECRET=UNA_CLAVE_LARGA_Y_SECRETA_DE_AL_MENOS_32_CARACTERES
JWT_EXPIRATION_MS=86400000

APP_ADMIN_USERNAME=admin
APP_ADMIN_EMAIL=admin@prode.local
APP_ADMIN_PASSWORD=Admin1234!
```

`application.properties` importa `.env` de forma opcional con:

```properties
spring.config.import=optional:file:.env[.properties]
```

## Ejecutar backend

Desde `prode/prode`:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

## Ejecutar tests

Desde `prode/prode`:

```powershell
.\mvnw.cmd test
```

## Arquitectura general

El backend esta organizado por feature-package:

- `auth`
- `user`
- `team`
- `matchday`
- `match`
- `prediction`
- `ranking`
- `group`
- `tournament`
- `common`
- `config`
- `security`
- `seed`

Los DTOs se implementan como `record`. Las entidades JPA usan Lombok con
`@Getter`, `@Setter` y `@Entity`. Los controllers exponen endpoints REST y la
seguridad se aplica con `@PreAuthorize`.

## Modelo por torneos

El flujo principal actual usa `tournamentId`.

Cada torneo puede tener:

- formato `LEAGUE` o `GROUPS`;
- equipos asociados;
- grupos opcionales por equipo;
- fechas propias;
- partidos propios;
- resultados propios;
- pronosticos propios;
- ranking propio;
- tabla deportiva propia;
- llave eliminatoria propia.

El modelo de torneos extiende el dominio existente: `Match` y `MatchDay`
pertenecen a un torneo. La capa `tournament` reutiliza entidades y repositorios
existentes.

## Auth y roles

Endpoints publicos:

| Metodo | Endpoint             | Descripcion |
| ------ | -------------------- | ----------- |
| POST   | `/api/auth/register` | Registrar usuario |
| POST   | `/api/auth/login`    | Login y obtencion de JWT |
| GET    | `/api/health`        | Health check |

Endpoints autenticados:

| Metodo | Endpoint       | Descripcion |
| ------ | -------------- | ----------- |
| GET    | `/api/auth/me` | Usuario autenticado |

Roles:

- `USER`: usuario comun que pronostica y consulta rankings/tablas.
- `ADMIN`: administra torneos, equipos, fixture, resultados y eliminatorias.

Los endpoints protegidos reciben:

```text
Authorization: Bearer TOKEN
```

## Endpoints principales por torneo

### Torneos

| Metodo | Endpoint | Rol | Descripcion |
| ------ | -------- | --- | ----------- |
| GET | `/api/tournaments` | Autenticado | Lista torneos |
| GET | `/api/tournaments/available` | Autenticado | Torneos disponibles para usuarios |
| GET | `/api/tournaments/{tournamentId}` | Autenticado | Detalle del torneo |
| POST | `/api/tournaments` | ADMIN | Crear torneo |
| PATCH | `/api/tournaments/{tournamentId}/status` | ADMIN | Cambiar estado |
| PATCH | `/api/tournaments/{tournamentId}/format` | ADMIN | Cambiar formato |

### Equipos por torneo

| Metodo | Endpoint | Rol |
| ------ | -------- | --- |
| GET | `/api/tournaments/{tournamentId}/teams` | Autenticado |
| POST | `/api/tournaments/{tournamentId}/teams` | ADMIN |
| POST | `/api/tournaments/{tournamentId}/teams/bulk` | ADMIN |
| PATCH | `/api/tournaments/{tournamentId}/teams/{tournamentTeamId}/group` | ADMIN |
| DELETE | `/api/tournaments/{tournamentId}/teams/{tournamentTeamId}` | ADMIN |

### Fechas por torneo

| Metodo | Endpoint | Rol |
| ------ | -------- | --- |
| GET | `/api/tournaments/{tournamentId}/match-days` | Autenticado |
| POST | `/api/tournaments/{tournamentId}/match-days` | ADMIN |
| POST | `/api/tournaments/{tournamentId}/match-days/bulk` | ADMIN |
| DELETE | `/api/tournaments/{tournamentId}/match-days/{matchDayId}` | ADMIN |

### Partidos y resultados por torneo

| Metodo | Endpoint | Rol |
| ------ | -------- | --- |
| GET | `/api/tournaments/{tournamentId}/matches` | Autenticado |
| POST | `/api/tournaments/{tournamentId}/matches` | ADMIN |
| POST | `/api/tournaments/{tournamentId}/matches/bulk` | ADMIN |
| PUT | `/api/tournaments/{tournamentId}/matches/{matchId}/result` | ADMIN |
| DELETE | `/api/tournaments/{tournamentId}/matches/{matchId}` | ADMIN |

### Pronosticos por torneo

| Metodo | Endpoint | Rol |
| ------ | -------- | --- |
| GET | `/api/tournaments/{tournamentId}/predictions/me` | Autenticado |
| POST | `/api/tournaments/{tournamentId}/predictions/matches/{matchId}` | Autenticado |

### Ranking por torneo

| Metodo | Endpoint | Rol |
| ------ | -------- | --- |
| GET | `/api/tournaments/{tournamentId}/rankings/global` | Autenticado |

### Tabla deportiva

| Metodo | Endpoint | Rol |
| ------ | -------- | --- |
| GET | `/api/tournaments/{tournamentId}/standings` | Autenticado |

### Eliminatorias

| Metodo | Endpoint | Rol | Descripcion |
| ------ | -------- | --- | ----------- |
| GET | `/api/tournaments/{tournamentId}/knockout` | Autenticado | Consultar llave |
| POST | `/api/tournaments/{tournamentId}/knockout/generate` | ADMIN | Generar primera ronda |
| POST | `/api/tournaments/{tournamentId}/knockout/advance` | ADMIN | Avanzar a siguiente ronda |

## Reglas de negocio principales

### Pronosticos

- Cada usuario tiene como maximo un pronostico por partido.
- Si vuelve a pronosticar el mismo partido, se actualiza.
- Solo se puede pronosticar partidos `POR_JUGARSE`.
- Se bloquea la carga cuando faltan 30 minutos o menos para el inicio.
- Los puntos se calculan al cargar el resultado real.

### Puntaje

| Caso | Puntos |
| ---- | ------ |
| Marcador exacto | 3 |
| Tendencia correcta | 1 |
| Error | 0 |

La logica esta aislada en `PredictionScoringService`.

### Ranking

El ranking ordena por:

1. puntos totales descendente;
2. aciertos exactos descendente;
3. pronostico mas temprano;
4. `userId` como desempate estable.

### Standings

La tabla deportiva de equipos ordena por:

1. puntos;
2. diferencia de gol;
3. goles a favor;
4. nombre del equipo.

La tabla representa la fase regular. Por eso solo cuenta:

- partidos con `phase = REGULAR`;
- partidos con `phase = null`, por compatibilidad con datos previos.

No cuenta partidos `KNOCKOUT`.

### Eliminatorias

`Match` tiene metadata de eliminatorias:

- `phase`: `REGULAR` o `KNOCKOUT`;
- `knockoutRound`: `ROUND_OF_16`, `QUARTER_FINAL`, `SEMI_FINAL`, `FINAL`;
- `bracketPosition`;
- `winnerTeam`.

Reglas:

- La generacion toma clasificados desde standings.
- `qualifiersCount` soportado: 4, 8 o 16.
- En `LEAGUE` cruza 1 vs N, 2 vs N-1.
- En `GROUPS` con 2 grupos y 2 clasificados cruza A1 vs B2 y B1 vs A2.
- Un partido `KNOCKOUT` no puede terminar empatado.
- Al cargar resultado `KNOCKOUT`, el backend setea `winnerTeam`.
- `advance` genera solo la siguiente ronda usando ganadores ordenados por
  `bracketPosition`.
- No se puede avanzar desde `FINAL`.
- No se puede avanzar si falta finalizar algun partido o falta `winnerTeam`.
- No se permite corregir una ronda anterior si ya existe una ronda posterior.

## Seguridad y datos sensibles

No se debe commitear:

- `.env`;
- secretos;
- `target`;
- dumps de base de datos;
- archivos locales del IDE.

`.gitignore` ya excluye esos archivos.

## Errores

Los errores se devuelven con formato uniforme:

```json
{
  "timestamp": "2026-06-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "No se permite empate en un partido eliminatorio",
  "path": "/api/tournaments/1/matches/10/result"
}
```

El frontend muestra `message` cuando existe.

## Comandos utiles

Backend:

```powershell
cd prode/prode
.\mvnw.cmd spring-boot:run
```

Tests:

```powershell
cd prode/prode
.\mvnw.cmd test
```

Health check:

```http
GET http://localhost:8080/api/health
```
