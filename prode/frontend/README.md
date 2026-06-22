# Prode - Frontend

Frontend React + Vite para el sistema Prode de Programacion 4 UTN FRVM.

La aplicacion consume la API Spring Boot mediante JWT y permite operar flujos de
usuario y administracion de torneos.

## Stack

- React 19.
- Vite.
- React Router.
- CSS propio con estetica dark premium.
- Fetch API para consumo REST.
- JWT en `localStorage`.

## Requisitos

- Node.js instalado.
- Backend corriendo en `http://localhost:8080`.
- Variable `VITE_API_URL` configurada.

## Variables de entorno

Crear un archivo `.env` local en `prode/frontend`:

```env
VITE_API_URL=http://localhost:8080
```

No subir `.env` al repositorio.

## Instalacion

Desde `prode/frontend`:

```powershell
npm install
```

## Ejecutar en desarrollo

```powershell
npm run dev
```

Vite inicia normalmente en:

```text
http://localhost:5173
```

Si el puerto esta ocupado, puede usar `5174` u otro puerto informado por Vite.

## Build

```powershell
npm run build
```

El build genera `dist`, que no debe commitearse.

## Servicios y JWT

Los services usan `VITE_API_URL` y envian el JWT en endpoints protegidos:

```text
Authorization: Bearer TOKEN
```

Si el backend devuelve `401`, los services limpian la sesion local y redirigen a
`/login` siguiendo el patron existente.

Los errores del backend se muestran usando el campo `message` cuando esta
disponible.

## Rutas principales USER

| Ruta | Descripcion |
| ---- | ----------- |
| `/login` | Login |
| `/registro` | Registro |
| `/dashboard` | Inicio de usuario |
| `/tournaments` | Torneos disponibles |
| `/tournaments/:tournamentId` | Home del torneo |
| `/tournaments/:tournamentId/matches` | Partidos y pronosticos |
| `/tournaments/:tournamentId/predictions` | Mis pronosticos |
| `/tournaments/:tournamentId/ranking` | Ranking del torneo |
| `/tournaments/:tournamentId/standings` | Tabla deportiva |
| `/tournaments/:tournamentId/knockout` | Llave eliminatoria |

## Rutas principales ADMIN

| Ruta | Descripcion |
| ---- | ----------- |
| `/admin` | Panel admin |
| `/admin/tournaments` | Gestion de torneos |
| `/admin/tournaments/:tournamentId` | Panel del torneo |
| `/admin/tournaments/:tournamentId/teams` | Equipos y grupos |
| `/admin/tournaments/:tournamentId/schedule` | Fechas y partidos |
| `/admin/tournaments/:tournamentId/results` | Carga de resultados |
| `/admin/tournaments/:tournamentId/knockout` | Administracion de eliminatorias |
| `/admin/teams` | Gestion global de equipos |

## Flujo USER

1. Iniciar sesion.
2. Entrar a `Torneos`.
3. Abrir un torneo disponible.
4. Ver partidos del torneo.
5. Cargar o actualizar pronosticos.
6. Consultar `Mis pronosticos`.
7. Consultar ranking del torneo.
8. Consultar tabla deportiva.
9. Consultar llave eliminatoria.

## Flujo ADMIN

1. Iniciar sesion con usuario ADMIN.
2. Entrar al panel admin.
3. Crear o abrir un torneo.
4. Cargar equipos y grupos.
5. Cargar fechas y partidos.
6. Activar el torneo si corresponde.
7. Cargar resultados de fase regular.
8. Consultar standings.
9. Generar la llave eliminatoria.
10. Cargar resultados eliminatorios.
11. Avanzar ronda.
12. Consultar la final generada.

## Pantallas destacadas

- Landing.
- Login y registro.
- Dashboard de usuario.
- Listado de torneos.
- Home de torneo.
- Partidos y pronosticos.
- Mis pronosticos.
- Ranking por torneo.
- Standings.
- Llave eliminatoria.
- Panel admin.
- Gestion de equipos, fixture, resultados y eliminatorias.

## Manejo de errores

La UI muestra errores con `ErrorMessage`. Ejemplos esperados:

- sesion expirada;
- usuario sin permisos de ADMIN;
- empate en partido eliminatorio;
- intento de avanzar una ronda incompleta;
- intento de generar una llave duplicada.

## Comandos utiles

Instalar dependencias:

```powershell
cd prode/frontend
npm install
```

Desarrollo:

```powershell
cd prode/frontend
npm run dev
```

Build:

```powershell
cd prode/frontend
npm run build
```
