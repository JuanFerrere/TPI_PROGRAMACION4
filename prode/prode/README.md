# Prode

API REST backend para una plataforma de pronosticos deportivos tipo Prode.

El proyecto esta desarrollado como trabajo practico de Programacion 4. Actualmente llega hasta la **Etapa 6: grupos privados y ranking por grupo**.

## Integrantes

- Ferrere, Juan Ignacio.
- Pereyra, Benjamin.
- Rochietti, Geremias.
- Acosta, Mateo.

## Tecnologias usadas

- Java 21.
- Maven.
- Spring Boot 3.4.12.
- Spring Web.
- Spring Data JPA.
- PostgreSQL Driver.
- Lombok.
- Spring Security.
- Validation.
- JWT con JJWT.
- dotenv-java.

## Requisitos para ejecutar

- JDK 21 instalado.
- IntelliJ IDEA.
- PostgreSQL instalado y en ejecucion.
- Base de datos `prode_db` creada.
- Archivo `.env` local creado a partir de `.env.example`.
- Maven Wrapper incluido en el proyecto.

## Crear la base PostgreSQL `prode_db`

1. Abrir pgAdmin4.
2. Conectarse al servidor local de PostgreSQL.
3. Crear una base de datos llamada `prode_db`.
4. Dejar como owner al usuario de PostgreSQL que se usa en `.env`.

## Configuracion con variables de entorno

El archivo `.env` guarda valores sensibles o propios de cada computadora. No debe subirse al repositorio.

Ejemplo:

```env
DB_URL=jdbc:postgresql://localhost:5432/prode_db
DB_USERNAME=postgres
DB_PASSWORD=TU_PASSWORD_REAL_DE_POSTGRES

JWT_SECRET=UNA_CLAVE_LARGA_Y_SECRETA_DE_AL_MENOS_32_CARACTERES
JWT_EXPIRATION_MS=86400000

APP_ADMIN_USERNAME=admin
APP_ADMIN_EMAIL=admin@prode.local
APP_ADMIN_PASSWORD=UNA_PASSWORD_SEGURA_PARA_EL_ADMIN
```

`application.properties` usa placeholders como `${DB_PASSWORD}` para leer esos valores sin hardcodear secretos.

El proyecto importa `.env` de forma opcional con `spring.config.import=optional:file:.env[.properties]`. Esto permite que la aplicacion y los tests resuelvan las mismas variables locales. Si `.env` no existe, Spring intenta leer variables reales del sistema operativo.

## Ejecutar desde IntelliJ

1. Abrir como proyecto Maven la carpeta:

```text
C:\Users\Juani Ferrere\Desktop\TPI PROGRAMACION\prode\prode
```

2. Crear `.env` en la raiz del proyecto.
3. Verificar que PostgreSQL este iniciado.
4. Verificar que exista la base `prode_db`.
5. Ejecutar la clase principal:

```text
src/main/java/ar/edu/utn/frvm/prode/ProdeApplication.java
```

6. La API queda disponible en:

```text
http://localhost:8080
```

## Etapa 1 - Base del proyecto

Se preparo el proyecto Spring Boot, la conexion a PostgreSQL, variables de entorno, `.env.example`, `.gitignore`, estructura de paquetes y README inicial.

## Etapa 2 - Auth, usuarios, roles y JWT

Se implemento:

- Entidad `User`.
- Enum `Role` con valores `USER` y `ADMIN`.
- Registro de usuarios.
- Login de usuarios.
- Generacion de JWT.
- Filtro para autenticar requests con header `Authorization`.
- Endpoint protegido `GET /api/auth/me`.
- Admin inicial precargado desde variables de entorno.
- Contrasenas con BCrypt.
- Manejo global inicial de errores.
- Endpoint publico `GET /api/health`.

JWT es un token firmado que el cliente envia en cada request protegida. El servidor valida la firma y reconstruye el usuario autenticado sin guardar sesion.

## Etapa 3 - Equipos, Fechas y Partidos

En esta etapa se implemento el nucleo inicial del dominio deportivo.

### Team

`Team` representa un equipo participante del Prode.

Reglas principales:

- Se guarda en la tabla `teams`.
- Tiene `id`, `name` y `createdAt`.
- `name` es obligatorio y unico.
- `createdAt` se completa automaticamente al crear.
- No se puede eliminar un equipo si esta asociado a un partido como local o visitante.

### MatchDay

`MatchDay` representa una fecha o jornada.

Reglas principales:

- Se guarda en la tabla `match_days`.
- Tiene `id`, `name`, `status` y `createdAt`.
- `name` es obligatorio y unico.
- Se crea con estado `PROGRAMADA`.
- El cliente no edita el estado manualmente.
- El backend recalcula el estado segun sus partidos.
- La respuesta no incluye una lista de partidos para evitar respuestas gigantes o recursividad JSON.

Estados de `MatchDayStatus`:

- `PROGRAMADA`: estado inicial de una fecha.
- `EN_JUEGO`: al menos un partido de la fecha esta en juego.
- `FINALIZADA`: todos los partidos de la fecha terminaron.

### Match

`Match` representa un partido.

Reglas principales:

- Se guarda en la tabla `matches`.
- Pertenece a una fecha mediante `matchDay`.
- Tiene un equipo local `homeTeam`.
- Tiene un equipo visitante `awayTeam`.
- Local y visitante deben ser equipos distintos.
- `startTime` es obligatorio y se maneja como `Instant` en UTC.
- Se crea con estado `POR_JUGARSE`.
- Solo se puede modificar si esta `POR_JUGARSE`.
- Solo se puede eliminar si esta `POR_JUGARSE`.
- No se puede modificar si ya tiene pronosticos asociados.
- No se puede eliminar si ya tiene pronosticos asociados.
- Solo se puede pasar a `EN_JUEGO` si esta `POR_JUGARSE`.
- Al iniciar un partido, la fecha contenedora se recalcula automaticamente.
- `homeGoals`, `awayGoals` y `resultTrend` pueden quedar `null` en esta etapa.

Estados de `MatchStatus`:

- `POR_JUGARSE`: partido todavia no iniciado.
- `EN_JUEGO`: partido iniciado por un ADMIN.
- `FINALIZADO`: partido terminado; se usara en etapa de resultados.

`ResultTrend`:

- `LOCAL`: gana el equipo local.
- `EMPATE`: empate.
- `VISITANTE`: gana el equipo visitante.

En esta etapa queda preparado para resultados futuros, pero todavia no se cargan resultados reales ni puntuacion.

## Etapa 4 - Pronosticos

En esta etapa se implemento el sistema de pronosticos para partidos.

Un pronostico representa lo que un usuario cree que va a pasar en un partido: goles del local y goles del visitante. A partir de esos goles, el backend calcula automaticamente la tendencia:

- `LOCAL`: el usuario pronostico que gana el local.
- `EMPATE`: el usuario pronostico empate.
- `VISITANTE`: el usuario pronostico que gana el visitante.

Reglas principales:

- Cada pronostico pertenece a un usuario autenticado.
- Cada pronostico pertenece a un partido.
- Un usuario solo puede tener un pronostico por partido.
- Si el usuario vuelve a enviar un pronostico para el mismo partido, se actualiza el existente.
- Esto se llama `upsert`: crear si no existe, actualizar si ya existe.
- No se permiten goles negativos.
- Solo se puede pronosticar un partido en estado `POR_JUGARSE`.
- No se puede crear ni modificar un pronostico desde 30 minutos antes del inicio del partido.
- La hora usada para validar el bloqueo es la hora del servidor con `Instant.now()`, no una hora enviada por el cliente.
- Los pronosticos de otros usuarios solo se pueden ver cuando ya cerro el periodo de carga.
- `points` inicia en `0` y queda preparado para Etapa 5.
- `exactHit` inicia en `false` y queda preparado para Etapa 5.

### Bloqueo de 30 minutos

La regla de bloqueo se calcula asi:

```text
horaLimite = startTime del partido - 30 minutos
```

Si la hora actual del servidor es igual o posterior a `horaLimite`, el backend rechaza la creacion o modificacion del pronostico.

Ejemplo:

```text
startTime = 2026-06-20T19:00:00Z
horaLimite = 2026-06-20T18:30:00Z
```

A partir de `18:30:00Z`, el pronostico queda bloqueado.

## Etapa 5 - Resultados, puntos y ranking global

En esta etapa se cierra el ciclo de vida del partido: se carga el resultado real, se calculan los puntos de todos los pronosticos y se arma el ranking global.

### Flujo de un partido

```text
POR_JUGARSE  ->  EN_JUEGO  ->  FINALIZADO
   crear         /start         /result
```

- `PATCH /api/matches/{id}/start`: el ADMIN inicia el partido (pasa a `EN_JUEGO`).
- `PATCH /api/matches/{id}/result`: el ADMIN carga el resultado real (pasa a `FINALIZADO`).

### Cargar resultado

El ADMIN envia solo los goles reales:

```json
{
  "homeGoals": 2,
  "awayGoals": 1
}
```

Cuando se carga el resultado, el backend hace todo lo siguiente de forma automatica:

1. Valida que el partido este `EN_JUEGO`.
2. Calcula la tendencia real (`resultTrend`): `LOCAL`, `EMPATE` o `VISITANTE`.
3. Guarda `homeGoals`, `awayGoals` y `resultTrend`.
4. Cambia el partido a `FINALIZADO`.
5. Calcula los puntos de **todos** los pronosticos del partido.
6. Actualiza `exactHit` de cada pronostico.
7. Recalcula el estado de la fecha contenedora.

Importante: el cliente nunca manda la tendencia ni los puntos. El backend no confia en calculos del cliente; solo recibe los goles reales.

### Regla de puntuacion

Comparando el pronostico del usuario contra el resultado real:

- **3 puntos** si acierta el resultado exacto (mismos goles de local y visitante).
- **1 punto** si no acierta el exacto pero si la tendencia (quien gano o si fue empate).
- **0 puntos** si no acierta ni el exacto ni la tendencia.

Ejemplo con resultado real `2-1` (tendencia `LOCAL`):

| Pronostico | Tendencia pronosticada | Puntos | exactHit |
| ---------- | ---------------------- | ------ | -------- |
| 2-1        | LOCAL                  | 3      | true     |
| 1-0        | LOCAL                  | 1      | false    |
| 0-1        | VISITANTE              | 0      | false    |

- `exactHit` indica si el usuario acerto el marcador exacto. Solo es `true` cuando suma 3 puntos.
- `resultTrend` es la tendencia REAL del partido, calculada por el backend a partir de los goles.

### Ranking global

```text
GET /api/rankings/global
```

- Disponible para cualquier usuario autenticado.
- Suma los puntos de todos los pronosticos de cada usuario.
- Cuenta los aciertos exactos (`exactHits`) y la cantidad de pronosticos (`predictionsCount`).
- Ordena por: mas puntos primero, luego mas aciertos exactos, luego `username` alfabetico como desempate estable.
- Asigna la posicion (`position`) empezando en 1.
- Si todavia no hay pronosticos, devuelve una lista vacia (no es un error).

Ejemplo de respuesta:

```json
[
  {
    "position": 1,
    "userId": 2,
    "username": "juan",
    "totalPoints": 9,
    "exactHits": 2,
    "predictionsCount": 5
  }
]
```

## Por que se usa Instant/UTC

Los partidos usan `Instant` porque representa un punto exacto del tiempo en UTC. Esto evita errores cuando distintas computadoras o servidores tienen zonas horarias distintas.

Ejemplo valido para Insomnia:

```json
{
  "startTime": "2026-06-20T19:00:00Z"
}
```

La `Z` significa UTC.

## Seguridad por roles

El proyecto usa JWT y Spring Security.

Endpoints publicos:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/health`

Endpoints para cualquier usuario autenticado (`USER` o `ADMIN`):

- `GET /api/auth/me`
- `GET /api/teams`
- `GET /api/teams/{id}`
- `GET /api/match-days`
- `GET /api/match-days/{id}`
- `GET /api/matches`
- `GET /api/matches/{id}`
- `POST /api/predictions/matches/{matchId}`
- `GET /api/predictions/me`
- `GET /api/predictions/matches/{matchId}`
- `GET /api/rankings/global`

Endpoints solo `ADMIN`:

- `POST /api/teams`
- `DELETE /api/teams/{id}`
- `POST /api/match-days`
- `PUT /api/match-days/{id}`
- `DELETE /api/match-days/{id}`
- `POST /api/matches`
- `PUT /api/matches/{id}`
- `PATCH /api/matches/{id}/start`
- `PATCH /api/matches/{id}/result`
- `DELETE /api/matches/{id}`

La seguridad administrativa se aplica con `@PreAuthorize("hasRole('ADMIN')")`. Los usuarios se cargan con autoridad `ROLE_ADMIN`, por eso `hasRole('ADMIN')` coincide con la configuracion real.

## Formato de errores

Los errores se devuelven con una estructura uniforme:

```json
{
  "timestamp": "2026-06-02T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "No se puede modificar un partido que ya comenzo",
  "path": "/api/matches/1"
}
```

## Pruebas de Auth en Insomnia

### Registrar usuario

```http
POST http://localhost:8080/api/auth/register
```

Body:

```json
{
  "username": "juan",
  "email": "juan@test.com",
  "password": "Password123!"
}
```

Esperado: `201 Created`, token JWT y datos publicos del usuario.

### Login de usuario

```http
POST http://localhost:8080/api/auth/login
```

Body:

```json
{
  "usernameOrEmail": "juan",
  "password": "Password123!"
}
```

Esperado: token JWT para usar en endpoints protegidos.

### Login del admin precargado

El admin se configura en `.env` con `APP_ADMIN_USERNAME`, `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`.

```http
POST http://localhost:8080/api/auth/login
```

Body de ejemplo si `APP_ADMIN_PASSWORD=Admin1234!`:

```json
{
  "usernameOrEmail": "admin",
  "password": "Admin1234!"
}
```

### Usar Bearer Token

En Insomnia, agregar este header en cada request protegida:

```text
Authorization: Bearer PEGAR_TOKEN_AQUI
```

### Probar endpoint protegido

```http
GET http://localhost:8080/api/auth/me
```

Esperado: datos publicos del usuario autenticado. Sin token debe responder `401 Unauthorized`.

## Pruebas en Insomnia

Antes de probar endpoints protegidos:

1. Ejecutar la app.
2. Loguearse como ADMIN.
3. Copiar el token.
4. En cada request protegida usar header:

```text
Authorization: Bearer TOKEN_ADMIN
```

### 1. Crear equipo Boca

```http
POST http://localhost:8080/api/teams
```

Body:

```json
{
  "name": "Boca Juniors"
}
```

Esperado: `201 Created` y equipo creado.

### 2. Crear equipo River

```http
POST http://localhost:8080/api/teams
```

Body:

```json
{
  "name": "River Plate"
}
```

### 3. Listar equipos

```http
GET http://localhost:8080/api/teams
```

Esperado: lista de equipos.

### 4. Buscar equipo por nombre

```http
GET http://localhost:8080/api/teams?name=boca
```

Esperado: lista filtrada.

### 5. Crear fecha

```http
POST http://localhost:8080/api/match-days
```

Body:

```json
{
  "name": "Fecha 1 - Fase de Grupos"
}
```

Esperado: fecha creada con `status` igual a `PROGRAMADA`.

### 6. Listar fechas

```http
GET http://localhost:8080/api/match-days
```

Esperado: lista de fechas.

### 7. Crear partido

```http
POST http://localhost:8080/api/matches
```

Body:

```json
{
  "matchDayId": 1,
  "homeTeamId": 1,
  "awayTeamId": 2,
  "startTime": "2026-06-20T19:00:00Z"
}
```

Esperado: partido creado con `status` igual a `POR_JUGARSE`.

### 8. Validar local y visitante iguales

```http
POST http://localhost:8080/api/matches
```

Body:

```json
{
  "matchDayId": 1,
  "homeTeamId": 1,
  "awayTeamId": 1,
  "startTime": "2026-06-20T19:00:00Z"
}
```

Esperado: `400 Bad Request` con mensaje indicando que local y visitante no pueden ser el mismo equipo.

### 9. Listar partidos

```http
GET http://localhost:8080/api/matches
```

Esperado: lista ordenada por `startTime` ascendente.

### 10. Listar partidos por fecha

```http
GET http://localhost:8080/api/matches?matchDayId=1
```

Esperado: partidos de esa fecha.

### 11. Iniciar partido

```http
PATCH http://localhost:8080/api/matches/1/start
```

Esperado: el partido pasa a `EN_JUEGO` y la fecha contenedora pasa a `EN_JUEGO`.

### 12. Ver estado de la fecha

```http
GET http://localhost:8080/api/match-days/1
```

Esperado: `status` igual a `EN_JUEGO`.

### 13. Intentar modificar partido iniciado

```http
PUT http://localhost:8080/api/matches/1
```

Body:

```json
{
  "homeTeamId": 1,
  "awayTeamId": 2,
  "startTime": "2026-06-21T19:00:00Z"
}
```

Esperado: `400 Bad Request` con mensaje indicando que no se puede modificar un partido que ya comenzo.

### 14. Probar permisos con USER

1. Registrar o loguearse como usuario `USER`.
2. Usar su token.
3. Intentar:

```http
POST http://localhost:8080/api/teams
```

Body:

```json
{
  "name": "Independiente"
}
```

Esperado: `403 Forbidden`.

## Pruebas de Pronosticos en Insomnia

Antes de probar pronosticos:

1. Ejecutar la app.
2. Registrar o loguear un usuario.
3. Copiar el token.
4. Usar el header:

```text
Authorization: Bearer TOKEN
```

### 1. Registrar usuario comun

```http
POST http://localhost:8080/api/auth/register
```

Body:

```json
{
  "username": "juan",
  "email": "juan@test.com",
  "password": "Password123!"
}
```

### 2. Login usuario comun

```http
POST http://localhost:8080/api/auth/login
```

Body:

```json
{
  "usernameOrEmail": "juan",
  "password": "Password123!"
}
```

### 3. Crear pronostico

```http
POST http://localhost:8080/api/predictions/matches/1
```

Body:

```json
{
  "predictedHomeGoals": 2,
  "predictedAwayGoals": 1
}
```

Esperado:

- Se crea el pronostico.
- `predictedTrend` queda `LOCAL`.
- `points` queda `0`.
- `exactHit` queda `false`.

### 4. Modificar pronostico existente

Volver a enviar un `POST` al mismo partido:

```http
POST http://localhost:8080/api/predictions/matches/1
```

Body:

```json
{
  "predictedHomeGoals": 1,
  "predictedAwayGoals": 1
}
```

Esperado:

- No se crea duplicado.
- Se actualiza el pronostico existente.
- `predictedTrend` queda `EMPATE`.

### 5. Listar mis pronosticos

```http
GET http://localhost:8080/api/predictions/me
```

Esperado: lista de pronosticos del usuario autenticado.

### 6. Filtrar mis pronosticos por estado de partido

```http
GET http://localhost:8080/api/predictions/me?matchStatus=POR_JUGARSE
```

Tambien se puede probar:

```http
GET http://localhost:8080/api/predictions/me?matchStatus=EN_JUEGO
```

```http
GET http://localhost:8080/api/predictions/me?matchStatus=FINALIZADO
```

### 7. Intentar pronosticar partido EN_JUEGO

Iniciar el partido con el endpoint administrativo:

```http
PATCH http://localhost:8080/api/matches/1/start
```

Luego intentar pronosticar:

```http
POST http://localhost:8080/api/predictions/matches/1
```

Esperado: `400 Bad Request` porque solo se pueden pronosticar partidos `POR_JUGARSE`.

### 8. Intentar pronosticar cuando faltan menos de 30 minutos

Crear o usar un partido cuyo `startTime` este a menos de 30 minutos de la hora actual del servidor.

```http
POST http://localhost:8080/api/predictions/matches/1
```

Esperado: `400 Bad Request` porque el periodo de carga ya esta bloqueado.

### 9. Intentar ver pronosticos de terceros antes del bloqueo

```http
GET http://localhost:8080/api/predictions/matches/1
```

Esperado: `400 Bad Request` con mensaje indicando que los pronosticos estaran disponibles cuando cierre el periodo de carga.

### 10. Ver pronosticos de un partido despues del bloqueo

Usar un partido cuyo cierre de carga ya haya vencido.

```http
GET http://localhost:8080/api/predictions/matches/1
```

Esperado: lista de pronosticos cargados para ese partido.

## Pruebas de Resultados y Ranking en Insomnia

Estas pruebas cierran el ciclo completo: iniciar partido, cargar resultado, ver puntos y consultar el ranking.

Preparacion sugerida:

1. Login ADMIN.
2. Crear equipos.
3. Crear fecha.
4. Crear partido.
5. Login USER y crear pronostico.
6. Login ADMIN para iniciar y finalizar el partido.

### 1. Iniciar partido (ADMIN)

```http
PATCH http://localhost:8080/api/matches/1/start
```

Esperado: el partido queda `EN_JUEGO`.

### 2. Cargar resultado (ADMIN)

```http
PATCH http://localhost:8080/api/matches/1/result
```

Body:

```json
{
  "homeGoals": 2,
  "awayGoals": 1
}
```

Esperado:

- El partido queda `FINALIZADO`.
- `resultTrend` queda `LOCAL`.
- `homeGoals` = 2.
- `awayGoals` = 1.

### 3. Verificar pronostico con puntos (USER)

```http
GET http://localhost:8080/api/predictions/me
```

Esperado, segun lo que haya pronosticado el usuario para un resultado real `2-1`:

- Pronostico 2-1: `points` = 3, `exactHit` = true.
- Pronostico 1-0: `points` = 1, `exactHit` = false.
- Pronostico 0-1: `points` = 0, `exactHit` = false.

### 4. Consultar ranking global

```http
GET http://localhost:8080/api/rankings/global
```

Esperado: lista ordenada por puntos. Si no hay pronosticos, lista vacia.

### 5. Intentar cargar resultado como USER

```http
PATCH http://localhost:8080/api/matches/1/result
```

Esperado: `403 Forbidden` porque solo ADMIN puede cargar resultados.

### 6. Intentar cargar resultado en partido POR_JUGARSE

Usar un partido recien creado que todavia no fue iniciado.

```http
PATCH http://localhost:8080/api/matches/2/result
```

Esperado: `400 Bad Request` con mensaje `Solo se pueden cargar resultados de partidos que estan EN_JUEGO`.

### 7. Intentar cargar resultado en partido FINALIZADO

Volver a cargar resultado en el partido del paso 2.

```http
PATCH http://localhost:8080/api/matches/1/result
```

Esperado: `400 Bad Request` con mensaje `No se puede cargar resultado porque el partido ya esta FINALIZADO`.

## Errores comunes

`401 Unauthorized`

Causa: falta token, token vencido o header mal escrito.

Solucion: usar `Authorization: Bearer TOKEN`.

`403 Forbidden`

Causa: el usuario esta autenticado, pero no tiene rol `ADMIN`.

Solucion: loguearse con el admin precargado para endpoints administrativos.

`400 Bad Request` al crear partido

Causa posible: local y visitante tienen el mismo id, falta algun id o falta `startTime`.

Solucion: enviar ids existentes y equipos distintos.

`400 Bad Request` al crear pronostico

Causa posible: el partido no esta `POR_JUGARSE`, faltan menos de 30 minutos para el inicio o los goles son negativos.

Solucion: usar un partido pendiente, fuera del periodo bloqueado y enviar goles mayores o iguales a cero.

`400 Bad Request` al ver pronosticos de un partido

Causa posible: todavia no cerro el periodo de carga del partido.

Solucion: esperar a que falten 30 minutos o menos para el inicio del partido.

`400 Bad Request` al cargar resultado

Causa posible: el partido no esta `EN_JUEGO` (todavia esta `POR_JUGARSE` o ya esta `FINALIZADO`) o los goles son negativos.

Solucion: iniciar el partido con `/start` antes de cargar el resultado y enviar goles mayores o iguales a cero.

`400 Bad Request` por fecha

Causa posible: `startTime` no esta en formato ISO-8601 UTC.

Solucion: usar un valor como `2026-06-20T19:00:00Z`.

`404 Not Found`

Causa: no existe el equipo, fecha o partido solicitado.

Solucion: revisar los ids creados previamente.

`database "prode_db" does not exist`

Causa: la base no fue creada.

Solucion: crear `prode_db` en PostgreSQL.

`password authentication failed for user "postgres"`

Causa: `DB_PASSWORD` esta mal.

Solucion: revisar `.env`.

`Could not resolve placeholder`

Causa: falta alguna variable de entorno o no se cargo `.env`.

Solucion: crear `.env` en la raiz del proyecto o configurar variables en IntelliJ.

## Explicacion para defensa oral

"En esta etapa cerramos el ciclo de vida del partido. El ADMIN inicia el partido y luego carga el resultado real enviando solo los goles; el backend nunca confia en calculos del cliente. Al cargar el resultado, el sistema calcula la tendencia real, finaliza el partido, recalcula los puntos de todos los pronosticos asociados (3 por resultado exacto, 1 por tendencia, 0 si no acierta), actualiza el acierto exacto y recalcula el estado de la fecha. Con esos puntos acumulados se arma el ranking global, que cualquier usuario autenticado puede consultar ordenado por puntos, aciertos exactos y nombre."

## Etapa 6 - Grupos privados y ranking por grupo

Los grupos privados permiten que un conjunto cerrado de usuarios compita entre si
dentro de un espacio propio, separado del ranking global. Cada grupo tiene un codigo
de invitacion unico generado por el backend: compartir ese codigo es la unica forma
de unirse.

### Que es un grupo privado

Un grupo privado agrupa a usuarios que se conocen (amigos, companeros de facultad,
familia). Solo los miembros del grupo pueden ver su detalle y su ranking. El codigo
de invitacion es una cadena de 8 caracteres hexadecimales en mayusculas, generada
con UUID y verificada como unica antes de guardarse. Ejemplo: `3F2504E0`.

### Endpoints de grupos

Todos requieren autenticacion JWT valida. El prefijo comun es `/api/groups`.

**Autenticados (USER y ADMIN):**

| Metodo   | Endpoint                          | Descripcion                                   |
|----------|-----------------------------------|-----------------------------------------------|
| POST     | /api/groups                       | Crear grupo privado                           |
| POST     | /api/groups/join                  | Unirse a grupo por codigo de invitacion       |
| GET      | /api/groups/me                    | Listar mis grupos                             |
| GET      | /api/groups/{groupId}             | Ver detalle de un grupo (solo miembros)       |
| DELETE   | /api/groups/{groupId}/members/me  | Salir de un grupo                             |
| GET      | /api/groups/{groupId}/ranking     | Ver ranking del grupo (solo miembros)         |

### Como crear un grupo

```http
POST http://localhost:8080/api/groups
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "name": "Amigos UTN"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "name": "Amigos UTN",
  "inviteCode": "3F2504E0",
  "ownerId": 2,
  "ownerUsername": "juan",
  "membersCount": 1,
  "createdAt": "2026-06-09T18:00:00Z"
}
```

El owner queda automaticamente como primer miembro del grupo.

### Como se genera el codigo de invitacion

El backend toma los primeros 8 caracteres del UUID generado (sin guiones), los
convierte a mayusculas y verifica que no exista en la base antes de guardar.
El cliente no puede elegir ni modificar el codigo.

### Como unirse a un grupo

```http
POST http://localhost:8080/api/groups/join
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "inviteCode": "3F2504E0"
}
```

Respuesta `200 OK` con el grupo al que se unio. Si el usuario ya pertenece al
grupo o el codigo no existe, se devuelve un error claro.

### Como listar mis grupos

```http
GET http://localhost:8080/api/groups/me
Authorization: Bearer TOKEN
```

Devuelve lista de todos los grupos donde el usuario autenticado es miembro.
Si no pertenece a ninguno, devuelve lista vacia (no es un error).

### Como ver el detalle de un grupo

```http
GET http://localhost:8080/api/groups/{groupId}
Authorization: Bearer TOKEN
```

Solo accesible para miembros del grupo. Devuelve nombre, codigo, owner, lista
completa de miembros con su username y fecha de ingreso.

```json
{
  "id": 1,
  "name": "Amigos UTN",
  "inviteCode": "3F2504E0",
  "ownerId": 2,
  "ownerUsername": "juan",
  "membersCount": 2,
  "members": [
    { "userId": 2, "username": "juan", "joinedAt": "2026-06-09T18:00:00Z" },
    { "userId": 3, "username": "benja", "joinedAt": "2026-06-09T18:05:00Z" }
  ],
  "createdAt": "2026-06-09T18:00:00Z"
}
```

### Como salir de un grupo

```http
DELETE http://localhost:8080/api/groups/{groupId}/members/me
Authorization: Bearer TOKEN
```

Devuelve `204 No Content` si salio correctamente. El owner no puede abandonar
su propio grupo (simplificacion del TPI; la transferencia de propiedad queda
como mejora futura).

### Como consultar el ranking por grupo

```http
GET http://localhost:8080/api/groups/{groupId}/ranking
Authorization: Bearer TOKEN
```

Solo accesible para miembros del grupo. Devuelve el ranking con solo los
usuarios del grupo. Los puntos provienen de los pronosticos ya calculados en
Etapa 5, no se recalculan. Todos los miembros aparecen aunque no hayan
pronosticado (con 0 puntos).

```json
[
  { "position": 1, "userId": 2, "username": "juan",  "totalPoints": 7, "exactHits": 2, "predictionsCount": 4 },
  { "position": 2, "userId": 3, "username": "benja", "totalPoints": 3, "exactHits": 1, "predictionsCount": 4 }
]
```

Orden: puntos descendente, luego aciertos exactos descendente, luego username
alfabetico como desempate estable.

### Validaciones y reglas de negocio

- El nombre del grupo no puede estar vacio ni superar 100 caracteres.
- El codigo de invitacion no puede estar vacio ni superar 50 caracteres.
- Un usuario no puede unirse dos veces al mismo grupo.
- Solo miembros pueden ver el detalle y el ranking del grupo.
- El owner no puede abandonar su propio grupo.
- Un usuario no miembro recibe error si intenta ver datos del grupo.

### Pruebas en Insomnia para Etapa 6

Flujo recomendado:

1. Login con juan (o el admin si es que juan es admin, si no registrar usuario juan).
2. Crear grupo:

```http
POST /api/groups
{ "name": "Amigos UTN" }
```

3. Copiar el `inviteCode` de la respuesta.
4. Registrar o loguearse con otro usuario, por ejemplo benja.
5. Benja se une al grupo:

```http
POST /api/groups/join
{ "inviteCode": "CODIGO_COPIADO" }
```

6. Verificar que ambos tienen pronosticos con puntos calculados (Etapa 5).
7. Consultar ranking global:

```http
GET /api/rankings/global
```

8. Consultar ranking del grupo:

```http
GET /api/groups/{groupId}/ranking
```

Verificar que solo aparecen juan y benja (no otros usuarios del sistema).

9. Con un tercer usuario que NO sea miembro, intentar ver el detalle:

```http
GET /api/groups/{groupId}
```

Esperado: `400 Bad Request` con mensaje `No tenes permisos para acceder a este grupo`.

10. Intentar que benja se una dos veces:

```http
POST /api/groups/join
{ "inviteCode": "CODIGO_COPIADO" }
```

Esperado: `400 Bad Request` con mensaje `El usuario ya pertenece a este grupo`.

11. Intentar que juan (owner) salga del grupo:

```http
DELETE /api/groups/{groupId}/members/me
(logueado como juan)
```

Esperado: `400 Bad Request` con mensaje `El creador del grupo no puede abandonar el grupo`.

12. Que benja salga del grupo:

```http
DELETE /api/groups/{groupId}/members/me
(logueado como benja)
```

Esperado: `204 No Content`.

### Errores comunes de Etapa 6

`400 Bad Request` al unirse

Causa posible: el codigo de invitacion no existe o el usuario ya pertenece al grupo.

Mensaje: `No existe un grupo con el codigo de invitacion indicado` o `El usuario ya pertenece a este grupo`.

`400 Bad Request` al ver detalle o ranking

Causa posible: el usuario no es miembro del grupo.

Mensaje: `No tenes permisos para acceder a este grupo` o `No tenes permisos para ver el ranking de este grupo`.

`400 Bad Request` al intentar salir siendo owner

Causa posible: el usuario que intenta salir es el creador del grupo.

Mensaje: `El creador del grupo no puede abandonar el grupo`.

`404 Not Found`

Causa: el groupId no existe en la base de datos.

Mensaje: `Grupo no encontrado`.

## Explicacion para defensa oral

"En Etapa 6 implementamos grupos privados. Un usuario crea un grupo y el backend genera un codigo de invitacion unico usando UUID. Ese codigo se comparte manualmente con otros usuarios, que lo usan para unirse. Solo los miembros del grupo pueden ver su detalle y su ranking. El ranking del grupo reutiliza los puntos ya calculados en Etapa 5 y filtra solo los miembros del grupo, por lo que refleja la competencia interna sin afectar el ranking global. El owner no puede abandonar su propio grupo para mantener la integridad del sistema dentro del alcance del TPI."

## Que queda preparado para futuras mejoras

- Los partidos ya tienen resultado real, tendencia y estado `FINALIZADO`.
- Los pronosticos ya tienen `points` y `exactHit` calculados automaticamente.
- El ranking global y por grupo ya funcionan.
- La logica de puntuacion esta aislada en `PredictionScoringService`.
- La estructura de grupos permite agregar en el futuro: transferencia de owner, eliminacion de grupo, invitaciones por link o por email.

No se implementaron todavia grupos privados, ranking por grupo ni frontend.
