# Prode

API REST backend para una plataforma de pronosticos deportivos tipo Prode.

El proyecto esta desarrollado como trabajo practico de Programacion 4. Actualmente llega hasta la **Etapa 4: sistema de pronosticos**.

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

Endpoints solo `ADMIN`:

- `POST /api/teams`
- `DELETE /api/teams/{id}`
- `POST /api/match-days`
- `PUT /api/match-days/{id}`
- `DELETE /api/match-days/{id}`
- `POST /api/matches`
- `PUT /api/matches/{id}`
- `PATCH /api/matches/{id}/start`
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

"En esta etapa implementamos el sistema de pronosticos. Cada usuario autenticado puede cargar un pronostico por partido y, si vuelve a enviarlo, se actualiza el existente para evitar duplicados. El backend calcula automaticamente la tendencia del pronostico y bloquea la carga 30 minutos antes del inicio usando la hora del servidor. Ademas, los pronosticos de otros usuarios quedan ocultos hasta que cierra el periodo de carga."

## Que queda preparado para Etapa 5

- Los partidos ya tienen estado y horario.
- Las fechas ya recalculan su estado desde sus partidos.
- Los pronosticos ya tienen `points` y `exactHit`.
- `ResultTrend`, `homeGoals` y `awayGoals` quedaron listos para resultados reales.
- La restriccion unica usuario + partido evita duplicados.

No se implementaron todavia resultados reales, motor de puntuacion final, rankings, grupos privados ni frontend.
