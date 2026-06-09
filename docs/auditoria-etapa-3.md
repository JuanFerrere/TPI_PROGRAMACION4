# Auditoría Etapa 1, 2 y 3

Auditoría técnica read-only del backend Spring Boot para el Trabajo Práctico Integrador de Programación 4 UTN.

Alcance revisado:

- Etapa 1: configuración base del proyecto.
- Etapa 2: autenticación, JWT, usuarios, roles y seguridad.
- Etapa 3: equipos, fechas/jornadas y partidos.

No se evaluaron como fallas graves los requisitos de pronósticos, resultados, rankings y grupos privados porque corresponden a etapas futuras.

## 1. Estado general

**Estado: Bien, pero con correcciones importantes.**

El código de Etapa 3 está bien encaminado: usa DTOs, services, repositories, validaciones, `Instant`, manejo global de errores y seguridad por roles con `@PreAuthorize`. La separación controller-service-repository es clara y no se observaron entidades devueltas directamente al cliente.

El problema más urgente no es de lógica, sino de entrega: los archivos nuevos de `team`, `matchday` y `match` están `untracked` en Git, por lo que no se subirían si el equipo entrega mediante commit o push. Además, el `pom.xml` usa Spring Boot `3.4.12`, mientras que el contexto informado del proyecto menciona `3.5.14`.

No se ejecutó Maven, compilación ni tests durante esta auditoría porque la revisión fue solicitada en modo estrictamente read-only. Tampoco se modificaron archivos del proyecto durante la revisión.

Puntos generales positivos:

- Package base correcto: `ar.edu.utn.frvm.prode`.
- Java 21 configurado en `pom.xml`.
- PostgreSQL configurado mediante variables de entorno.
- `JWT_SECRET`, `DB_PASSWORD` y `APP_ADMIN_PASSWORD` no están hardcodeados en `application.properties`.
- `.env` está ignorado por Git.
- `.env.example` está permitido para subir como plantilla.
- BCrypt está configurado mediante `PasswordEncoder`.
- Seguridad stateless con JWT.
- Manejo global de errores con formato uniforme.
- Etapa 3 usa `Instant` para fechas críticas.
- README documenta el estado actual del proyecto y las pruebas principales de Etapa 3.

## 2. Problemas críticos

### 2.1 Archivos de Etapa 3 sin seguimiento en Git

Los archivos nuevos de Etapa 3 aparecen como `untracked`. Esto significa que IntelliJ los ve localmente, pero Git no los incluiría en un commit salvo que se agreguen explícitamente.

Impacto:

- La entrega puede quedar sin equipos, fechas y partidos.
- Otro integrante o docente podría clonar el repositorio y no ver la Etapa 3.
- El proyecto podría parecer incompleto aunque funcione en la máquina local.

Archivos afectados:

- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/...`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/...`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/...`

Recomendación:

- Revisar los archivos nuevos.
- Confirmar que pertenecen a Etapa 3.
- Luego agregarlos a Git de forma explícita antes de entregar.

No se recomienda hacer `git add .` sin revisar, porque podría incluir archivos no deseados.

### 2.2 Inconsistencia de versión de Spring Boot

El contexto del proyecto indica Spring Boot `3.5.14`, pero el `pom.xml` y el README declaran Spring Boot `3.4.12`.

Impacto:

- Puede generar observaciones en la defensa si la consigna exige una versión concreta.
- Puede confundir a otros integrantes.
- Puede producir diferencias de comportamiento si se actualiza sin probar.

Recomendación:

- Confirmar con la consigna o con la cátedra si la versión debe ser exactamente `3.5.14`.
- Si no hay exigencia estricta, mantener `3.4.12` y ajustar el contexto/documentación.
- Si sí hay exigencia, actualizar versión y ejecutar compilación/tests antes de entregar.

### 2.3 Falta de pruebas específicas para Etapa 3

Actualmente solo se observó un test base tipo `contextLoads`.

Impacto:

- No hay verificación automática de permisos ADMIN/USER.
- No hay verificación automática de reglas de negocio.
- Cambios futuros en pronósticos o resultados podrían romper Etapa 3 sin detectarlo.

Recomendación:

- Agregar tests mínimos antes de Etapa 4, especialmente sobre seguridad y reglas de negocio.

## 3. Problemas medios

### 3.1 Falta `@Positive` en IDs de DTOs

Los DTOs de partidos validan que los IDs no sean `null`, pero no validan que sean positivos.

Ejemplos:

- `matchDayId`
- `homeTeamId`
- `awayTeamId`

Impacto:

- Un cliente podría enviar `0` o valores negativos.
- Probablemente terminaría en `404`, pero el error semánticamente correcto sería `400 Bad Request`.

Recomendación:

- Agregar `@Positive` en IDs de entrada.

### 3.2 Se puede crear un partido en una fecha ya iniciada

`createMatch` valida fecha existente, equipos distintos y horario, pero no valida explícitamente que la fecha esté `PROGRAMADA`.

Impacto:

- Un ADMIN podría agregar partidos a una fecha que ya está `EN_JUEGO`.
- Puede complicar reglas futuras de pronósticos y rankings.

Recomendación:

- Definir la regla de negocio antes de Etapa 4.
- Si la fecha ya está `EN_JUEGO` o `FINALIZADA`, probablemente no debería aceptar nuevos partidos.

### 3.3 `updatedAt` podría devolverse desactualizado en algunas respuestas

`updatedAt` se actualiza con `@PreUpdate`, pero algunos DTOs se construyen antes del flush de JPA.

Impacto:

- La base termina correcta, pero la respuesta inmediata puede mostrar el `updatedAt` anterior.
- No rompe la lógica principal, pero puede confundir en Insomnia.

Recomendación:

- Si se quiere exactitud inmediata en la respuesta, actualizar manualmente `updatedAt` en service o forzar flush de forma controlada.
- No es urgente para Etapa 3 si no se evalúa ese campo.

### 3.4 Username/email sensibles a mayúsculas

El registro valida duplicados con `existsByUsername` y `existsByEmail`, que son sensibles al valor exacto.

Impacto:

- Podrían existir usuarios como `Juan` y `juan`.
- Podrían existir emails equivalentes con distinta capitalización.

Recomendación:

- Normalizar username/email o validar duplicados ignorando mayúsculas.
- Para email, conviene guardar en lowercase.

### 3.5 JWT secret sin validación explícita

El proyecto lee `JWT_SECRET` desde variable de entorno, lo cual está bien. Sin embargo, no hay una validación propia que explique claramente si la clave es demasiado corta.

Impacto:

- JJWT puede fallar al firmar si la clave no cumple longitud mínima.
- El error puede ser menos claro para estudiantes o durante la defensa.

Recomendación:

- Validar longitud mínima de `JWT_SECRET` al inicializar `JwtService`.
- Devolver un mensaje claro en logs si está mal configurado.

### 3.6 Password del admin inicial sin validación fuerte

`DataSeeder` verifica que `APP_ADMIN_PASSWORD` no esté vacío, pero no valida longitud ni complejidad.

Impacto:

- El admin inicial podría tener una contraseña débil.

Recomendación:

- Validar mínimo 8 caracteres o documentar claramente que debe ser segura.

## 4. Mejoras menores

### 4.1 Mappers dentro de services

Actualmente los métodos `toResponse` están dentro de los services.

Estado:

- Aceptable para Etapa 3.
- Puede volverse incómodo en Etapa 4 y Etapa 5.

Recomendación:

- Si el proyecto crece mucho, crear mappers dedicados.
- No es necesario hacerlo antes de entregar Etapa 3.

### 4.2 README puede reforzar Auth

El README documenta bien Etapa 3, pero puede mejorar con ejemplos explícitos de:

- `POST /api/auth/register`
- `POST /api/auth/login`
- Login con ADMIN precargado.
- Uso del token en Insomnia.

Estado:

- No es crítico porque la documentación general está bien.
- Sí ayudaría en la defensa y en pruebas de terceros.

### 4.3 `spring.jpa.show-sql=true`

Está activado para desarrollo.

Estado:

- Correcto para cursada y debugging.
- No recomendado para producción.

Recomendación:

- Mantenerlo si la cátedra quiere ver SQL.
- Si se preparan perfiles, moverlo a perfil `dev`.

### 4.4 Redundancia entre `dotenv-java` y `spring.config.import`

El proyecto usa `dotenv-java` en la clase principal y también `spring.config.import=optional:file:.env[.properties]`.

Estado:

- Funciona y es entendible.
- Hay cierta redundancia.

Recomendación:

- No tocar ahora si funciona.
- En una etapa futura, elegir una estrategia única para simplificar.

### 4.5 Comentarios abundantes

El código está muy comentado, lo cual es bueno para un TPI universitario.

Riesgo menor:

- Hay comentarios en líneas bastante obvias.

Recomendación:

- Para defensa está bien.
- En un proyecto profesional se dejarían solo comentarios de intención, reglas de negocio y decisiones técnicas.

## 5. Cumplimiento Etapa 3

### 5.1 Equipos

| Regla | Estado | Comentario |
| --- | --- | --- |
| Crear equipos | Cumple | `POST /api/teams` protegido para ADMIN. |
| Nombre obligatorio | Cumple | DTO usa `@NotBlank`. |
| Nombre único | Cumple | Repositorio valida ignorando mayúsculas. |
| `createdAt` automático | Cumple | Se usa `@PrePersist` con `Instant`. |
| Listar equipos | Cumple | `GET /api/teams`. |
| Buscar por nombre | Cumple | Query param `name`. |
| Obtener por id | Cumple | `GET /api/teams/{id}`. |
| Eliminar equipo solo si no tiene partidos | Cumple | Valida local y visitante. |
| Endpoints ADMIN protegidos | Cumple | `@PreAuthorize("hasRole('ADMIN')")`. |
| GET autenticado | Cumple | `@PreAuthorize("isAuthenticated()")`. |

Evaluación:

- La implementación de equipos está sólida para Etapa 3.
- La validación contra duplicados está bien planteada.

### 5.2 Fechas / Jornadas

| Regla | Estado | Comentario |
| --- | --- | --- |
| Crear fechas | Cumple | `POST /api/match-days`. |
| Nombre obligatorio | Cumple | DTO usa `@NotBlank`. |
| Nombre único | Cumple | Valida ignorando mayúsculas. |
| Estado inicial `PROGRAMADA` | Cumple | Entity y service lo aseguran. |
| Cliente no edita status | Cumple | DTO de update solo permite `name`. |
| Listar fechas | Cumple | `GET /api/match-days`. |
| Filtrar por estado | Cumple | Query param `status`. |
| Obtener por id | Cumple | `GET /api/match-days/{id}`. |
| Modificar solo si corresponde | Cumple | Solo `PROGRAMADA` y sin partidos. |
| Eliminar solo si corresponde | Cumple | Solo `PROGRAMADA` y sin partidos. |
| Recalcular estado | Cumple | Service recalcula según partidos. |

Regla de estado esperada:

- Sin partidos o todos `POR_JUGARSE`: `PROGRAMADA`.
- Al menos uno `EN_JUEGO`: `EN_JUEGO`.
- Todos `FINALIZADO`: `FINALIZADA`.

Evaluación:

- La implementación está bien para Etapa 3.
- La restricción de no modificar/eliminar fechas con partidos es conservadora y correcta para integridad.

### 5.3 Partidos

| Regla | Estado | Comentario |
| --- | --- | --- |
| Tiene fecha | Cumple | Relación `ManyToOne` con `MatchDay`. |
| Tiene local | Cumple | Relación `ManyToOne` con `Team`. |
| Tiene visitante | Cumple | Relación `ManyToOne` con `Team`. |
| Tiene `startTime` | Cumple | Campo obligatorio `Instant`. |
| Usa UTC | Cumple | `Instant` representa un instante UTC. |
| Estado inicial `POR_JUGARSE` | Cumple | Entity y service lo aseguran. |
| Campos de resultado preparados | Cumple | `homeGoals`, `awayGoals`, `resultTrend`. |
| `createdAt` y `updatedAt` automáticos | Cumple | `@PrePersist` y `@PreUpdate`. |
| Local y visitante distintos | Cumple | Validación en service. |
| Modificar solo `POR_JUGARSE` | Cumple | Regla explícita. |
| Pasar a `EN_JUEGO` | Cumple | `PATCH /api/matches/{id}/start`. |
| Recalcular fecha al iniciar | Cumple | Llama a refresh de MatchDay. |
| Listar ordenado por horario | Cumple | Repositorio ordena por `startTime`. |
| Filtrar por `matchDayId` | Cumple | Query param. |
| Eliminar solo `POR_JUGARSE` | Cumple | Regla explícita. |
| TODO para pronósticos | Cumple | Hay TODO para `PredictionRepository`. |

Evaluación:

- La base de partidos está muy bien preparada para pronósticos y resultados.
- Conviene definir antes de Etapa 4 si se permite agregar partidos a una fecha ya iniciada.

### 5.4 Seguridad de Etapa 3

| Endpoint | Estado esperado | Estado observado |
| --- | --- | --- |
| `GET /api/teams` | USER o ADMIN autenticado | Cumple |
| `GET /api/teams/{id}` | USER o ADMIN autenticado | Cumple |
| `POST /api/teams` | Solo ADMIN | Cumple |
| `DELETE /api/teams/{id}` | Solo ADMIN | Cumple |
| `GET /api/match-days` | USER o ADMIN autenticado | Cumple |
| `GET /api/match-days/{id}` | USER o ADMIN autenticado | Cumple |
| `POST /api/match-days` | Solo ADMIN | Cumple |
| `PUT /api/match-days/{id}` | Solo ADMIN | Cumple |
| `DELETE /api/match-days/{id}` | Solo ADMIN | Cumple |
| `GET /api/matches` | USER o ADMIN autenticado | Cumple |
| `GET /api/matches/{id}` | USER o ADMIN autenticado | Cumple |
| `POST /api/matches` | Solo ADMIN | Cumple |
| `PUT /api/matches/{id}` | Solo ADMIN | Cumple |
| `PATCH /api/matches/{id}/start` | Solo ADMIN | Cumple |
| `DELETE /api/matches/{id}` | Solo ADMIN | Cumple |

## 6. Archivos que deberían subirse

Estos archivos deberían estar incluidos en Git para que Etapa 3 exista en la entrega:

### Team

- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/controller/TeamController.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/dto/TeamCreateRequest.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/dto/TeamResponse.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/entity/Team.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/repository/TeamRepository.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/team/service/TeamService.java`

### MatchDay

- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/controller/MatchDayController.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/dto/MatchDayCreateRequest.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/dto/MatchDayResponse.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/dto/MatchDayUpdateRequest.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/entity/MatchDay.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/entity/MatchDayStatus.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/repository/MatchDayRepository.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/matchday/service/MatchDayService.java`

### Match

- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/controller/MatchController.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/dto/MatchCreateRequest.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/dto/MatchResponse.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/dto/MatchUpdateRequest.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/entity/Match.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/entity/MatchStatus.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/entity/ResultTrend.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/repository/MatchRepository.java`
- `prode/prode/src/main/java/ar/edu/utn/frvm/prode/match/service/MatchService.java`

### Documentación y configuración segura

- `prode/prode/.env.example`
- `prode/prode/.gitignore`
- `prode/prode/README.md`
- `prode/prode/pom.xml`
- `prode/prode/src/main/resources/application.properties`
- `prode/prode/mvnw`
- `prode/prode/mvnw.cmd`
- `prode/prode/.mvn/wrapper/maven-wrapper.properties`

Nota:

- El wrapper está configurado con `distributionType=only-script`, por lo que no se observó como problema que `maven-wrapper.jar` no esté trackeado.

## 7. Archivos que NO deberían subirse

Estos archivos o carpetas no deberían subirse:

- `prode/prode/.env`
- `prode/prode/target/`
- `.idea/`
- `*.iml`
- `.vscode/`
- `build/`
- Archivos con contraseñas reales.
- Dumps de base de datos con datos reales.
- Logs con tokens JWT o passwords.

Observación importante:

- El `.env` local existe y está correctamente ignorado.
- No debe compartirse por Discord, mail, Drive ni GitHub.
- `.env.example` sí debe subirse porque es una plantilla sin secretos reales.

## 8. Recomendaciones antes de Etapa 4

### 8.1 Antes de tocar pronósticos

Recomendaciones:

- Asegurar que todos los archivos de Etapa 3 estén trackeados por Git.
- Confirmar versión de Spring Boot.
- Ejecutar compilación y tests con autorización del equipo.
- Agregar tests mínimos para reglas actuales.
- Definir si se puede crear partido en fecha `EN_JUEGO`.

### 8.2 Tests mínimos recomendados

Auth y seguridad:

- Registro exitoso.
- Login exitoso.
- Login con credenciales inválidas.
- `/api/auth/me` sin token devuelve 401.
- USER intentando `POST /api/teams` devuelve 403.
- ADMIN puede crear equipo.

Equipos:

- Crear equipo.
- No crear equipo duplicado.
- Listar equipos.
- Buscar equipo por nombre.
- No eliminar equipo asociado a partido.

Fechas:

- Crear fecha con estado `PROGRAMADA`.
- No crear fecha duplicada.
- Filtrar por estado.
- No modificar fecha con partidos.
- No eliminar fecha con partidos.

Partidos:

- Crear partido.
- Rechazar local y visitante iguales.
- Listar ordenado por `startTime`.
- Filtrar por `matchDayId`.
- Iniciar partido.
- Recalcular fecha a `EN_JUEGO`.
- No modificar partido iniciado.
- No eliminar partido iniciado.

### 8.3 Preparación para pronósticos

Para Etapa 4 conviene diseñar:

- Entidad `Prediction`.
- Relación `Prediction -> User`.
- Relación `Prediction -> Match`.
- Constraint único `user_id + match_id`.
- Bloqueo de pronóstico 30 minutos antes del partido.
- Validación con `Instant.now()` y `match.startTime.minus(30, ChronoUnit.MINUTES)`.
- Privacidad: un usuario no debería ver pronósticos de terceros antes de resultados.

### 8.4 Preparación para resultados y puntuación

Para Etapa 5 conviene diseñar:

- Endpoint ADMIN para cargar resultado.
- Validar goles no negativos.
- Calcular `ResultTrend`.
- Cambiar partido a `FINALIZADO`.
- Recalcular estado de fecha.
- Motor de puntuación separado del controller.
- Ranking global derivado de pronósticos y resultados.

### 8.5 Preparación para grupos privados

Para Etapa 6 conviene diseñar:

- Entidad `Group`.
- Entidad intermedia de miembros.
- Código de invitación único.
- Owner/admin del grupo.
- Ranking por grupo.
- Validaciones para evitar acceso a grupos ajenos.

### 8.6 Prompt seguro para corrección futura

Si se decide corregir los puntos detectados, usar un pedido acotado como este:

```text
Actuá como desarrollador backend senior Spring Boot y docente de Programación 4 UTN.

Necesito corregir solo los problemas confirmados de la auditoría read-only, sin avanzar a Etapa 4 y sin agregar funcionalidades nuevas.

Restricciones:
- No refactorizar arquitectura completa.
- No tocar lógica de negocio salvo para corregir riesgos confirmados.
- No modificar seguridad funcional salvo validaciones puntuales.
- No borrar código útil.
- No hacer commits.
- Antes de editar, mostrar plan breve y esperar confirmación.

Correcciones a considerar:
1. Verificar archivos untracked de Etapa 3 y proponer `git add` exacto, sin ejecutarlo salvo autorización.
2. Confirmar si Spring Boot debe ser 3.4.12 o 3.5.14 antes de tocar `pom.xml` y README.
3. Agregar `@Positive` en IDs de DTOs de partidos si se confirma.
4. Agregar ejemplos de register/login en README si se confirma.
5. Evaluar regla para impedir crear partidos en fechas no PROGRAMADA, pero no implementarla sin confirmación.
6. Proponer tests mínimos de Etapa 3, pero no crearlos sin confirmación.

Primero revisá el estado actual y devolvé un plan de cambios seguro.
```
