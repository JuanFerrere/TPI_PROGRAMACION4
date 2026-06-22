# Guia de demo - Prode

Guia para preparar y presentar el sistema Prode en la defensa del TPI de
Programacion 4 UTN FRVM.

## 1. Requisitos previos

- JDK 21.
- Node.js.
- PostgreSQL iniciado.
- Base `prode_db` creada.
- Backend con `.env` local en `prode/prode`.
- Frontend con `.env` local en `prode/frontend`.
- Rama `master` actualizada.

## 2. PostgreSQL

1. Abrir pgAdmin o consola PostgreSQL.
2. Verificar que el servidor local este iniciado.
3. Crear la base si no existe:

```sql
CREATE DATABASE prode_db;
```

4. Confirmar que el usuario configurado en `.env` tenga acceso.

## 3. Backend

Archivo esperado:

```text
prode/prode/.env
```

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

Levantar backend:

```powershell
cd prode/prode
.\mvnw.cmd spring-boot:run
```

La API queda en:

```text
http://localhost:8080
```

## 4. Frontend

Archivo esperado:

```text
prode/frontend/.env
```

Contenido:

```env
VITE_API_URL=http://localhost:8080
```

Instalar dependencias si hace falta:

```powershell
cd prode/frontend
npm install
```

Levantar frontend:

```powershell
cd prode/frontend
npm run dev
```

Abrir la URL informada por Vite, normalmente:

```text
http://localhost:5173
```

Si `5173` esta ocupado, Vite puede usar `5174`.

## 5. Credenciales demo

Completar con las credenciales locales disponibles antes de la defensa.

ADMIN:

```text
usuario:
password:
```

USER:

```text
usuario:
password:
```

Usuarios demo sugeridos si existen en la base:

```text
demo_ana / Demo1234!
demo_beto / Demo1234!
demo_caro / Demo1234!
```

Torneo demo sugerido si existe:

```text
Copa Demo 2026
```

## 6. Flujo ADMIN

### 6.1 Login

1. Entrar a `/login`.
2. Iniciar sesion con usuario ADMIN.
3. Ir a `/admin`.

### 6.2 Crear o abrir torneo

1. Entrar a `/admin/tournaments`.
2. Crear un torneo o abrir uno existente.
3. Verificar formato `LEAGUE` o `GROUPS`.
4. Verificar estado `ACTIVE` antes de generar eliminatorias.

### 6.3 Cargar equipos

1. Entrar a `/admin/tournaments/{id}/teams`.
2. Agregar equipos.
3. Si el torneo es `GROUPS`, asignar grupos.

### 6.4 Cargar fechas y partidos

1. Entrar a `/admin/tournaments/{id}/schedule`.
2. Crear fechas.
3. Crear partidos de fase regular.
4. Verificar que los partidos queden `REGULAR`.

### 6.5 Cargar resultados de fase regular

1. Entrar a `/admin/tournaments/{id}/results`.
2. Cargar marcadores.
3. Confirmar que los partidos quedan `FINALIZADO`.
4. El backend recalcula puntos de pronosticos.

### 6.6 Ver standings

1. Entrar como usuario o admin a `/tournaments/{id}/standings`.
2. Verificar puntos, goles y diferencia de gol.
3. Aclarar en defensa: standings cuenta solo fase regular.

### 6.7 Generar llave eliminatoria

1. Entrar a `/admin/tournaments/{id}/knockout`.
2. Completar:
   - clasificados: `4`, `8` o `16`;
   - clasificados por grupo si aplica;
   - fecha/hora de primera ronda.
3. Presionar `Generar llave`.
4. Ver la primera ronda creada.

### 6.8 Cargar resultados eliminatorios

1. En la misma pantalla admin de eliminatorias, cargar goles.
2. Mostrar que el ganador se destaca despues de guardar.
3. Aclarar: el frontend no calcula ganador; lo devuelve el backend como
   `winnerTeam`.

### 6.9 Avanzar ronda

1. Finalizar todos los partidos de la ronda actual.
2. Completar fecha/hora de siguiente ronda.
3. Presionar `Avanzar ronda`.
4. Ver la nueva ronda creada con los ganadores.

## 7. Flujo USER

### 7.1 Login

1. Entrar a `/login`.
2. Iniciar sesion con usuario comun.
3. Ir a `/dashboard`.

### 7.2 Torneos

1. Entrar a `/tournaments`.
2. Abrir un torneo disponible.

### 7.3 Partidos y pronosticos

1. Entrar a `/tournaments/{id}/matches`.
2. Cargar pronostico para un partido `POR_JUGARSE`.
3. Ver validaciones de goles y bloqueo si aplica.

### 7.4 Mis pronosticos

1. Entrar a `/tournaments/{id}/predictions`.
2. Ver pronosticos cargados, resultados y puntos.

### 7.5 Ranking

1. Entrar a `/tournaments/{id}/ranking`.
2. Mostrar posiciones de usuarios.
3. Explicar desempates: puntos, exactos, pronostico mas temprano.

### 7.6 Standings

1. Entrar a `/tournaments/{id}/standings`.
2. Mostrar tabla deportiva de equipos.
3. Explicar desempates: puntos, DG, GF, nombre.

### 7.7 Llave eliminatoria

1. Entrar a `/tournaments/{id}/knockout`.
2. Mostrar rondas, resultados y ganadores.
3. Explicar que los partidos eliminatorios no contaminan standings.

## 8. Errores esperados para mostrar

### USER intentando acceder a admin

1. Loguearse como USER.
2. Intentar entrar a `/admin`.
3. Resultado esperado: redireccion o bloqueo de acceso segun pantalla.

### Empate en KNOCKOUT

Request o UI:

```json
{
  "homeGoals": 1,
  "awayGoals": 1
}
```

Resultado esperado:

```text
No se permite empate en un partido eliminatorio
```

### Avanzar ronda incompleta

1. Dejar un partido de la ronda sin finalizar.
2. Intentar `Avanzar ronda`.
3. Resultado esperado: error indicando que todos los partidos deben estar
   finalizados o que falta ganador.

### Generar llave duplicada

1. Generar la primera llave.
2. Intentar generar otra vez.
3. Resultado esperado:

```text
El torneo ya tiene una llave eliminatoria generada
```

## 9. Endpoints opcionales para Insomnia

Login:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin",
  "password": "Admin1234!"
}
```

Consultar standings:

```http
GET http://localhost:8080/api/tournaments/1/standings
Authorization: Bearer TOKEN
```

Consultar llave:

```http
GET http://localhost:8080/api/tournaments/1/knockout
Authorization: Bearer TOKEN
```

Generar llave:

```http
POST http://localhost:8080/api/tournaments/1/knockout/generate
Authorization: Bearer TOKEN_ADMIN
Content-Type: application/json

{
  "qualifiersCount": 4,
  "qualifiedPerGroup": 2,
  "firstRoundStartTime": "2026-07-01T20:00:00Z"
}
```

Cargar resultado eliminatorio:

```http
PUT http://localhost:8080/api/tournaments/1/matches/10/result
Authorization: Bearer TOKEN_ADMIN
Content-Type: application/json

{
  "homeGoals": 2,
  "awayGoals": 1
}
```

Avanzar ronda:

```http
POST http://localhost:8080/api/tournaments/1/knockout/advance
Authorization: Bearer TOKEN_ADMIN
Content-Type: application/json

{
  "nextRoundStartTime": "2026-07-05T20:00:00Z"
}
```

## 10. Mensaje de cierre para defensa

El sistema modela un Prode por torneos. Los usuarios pronostican partidos, el
admin carga resultados, el backend calcula puntos y ranking. La tabla deportiva
ordena equipos de fase regular y excluye eliminatorias. Luego se genera una
llave knockout desde standings, se cargan resultados sin empate, se guarda el
ganador y se avanza ronda automaticamente.
