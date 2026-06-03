# Prode

API REST backend para una plataforma de pronosticos deportivos tipo Prode.

El objetivo final del sistema es permitir usuarios, roles, partidos, pronosticos, resultados, rankings y grupos privados. Actualmente el proyecto llega hasta Etapa 2: autenticacion base con usuarios, roles y JWT.

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
- JJWT.
- dotenv-java.

## Requisitos para ejecutar

- JDK 21 instalado.
- IntelliJ IDEA.
- PostgreSQL instalado y en ejecucion.
- pgAdmin4 para administrar PostgreSQL.
- Base de datos `prode_db` creada.
- Archivo `.env` local creado a partir de `.env.example`.
- Maven Wrapper incluido en el proyecto.

## Crear la base PostgreSQL `prode_db`

1. Abrir pgAdmin4.
2. Conectarse al servidor local de PostgreSQL.
3. Hacer click derecho en `Databases`.
4. Seleccionar `Create` y luego `Database`.
5. En `Database`, escribir `prode_db`.
6. En `Owner`, dejar `postgres`.
7. Guardar.

## Configuracion segura con variables de entorno

Una variable de entorno es un valor de configuracion que vive fuera del codigo. Sirve para guardar datos que cambian entre computadoras, como usuarios, passwords, URLs o claves secretas.

No se deben subir contrasenas ni claves reales a GitHub porque cualquier persona con acceso al repositorio podria usarlas.

El archivo `.env` es un archivo local de cada integrante. Ahi van los valores reales para ejecutar el proyecto en su computadora.

El archivo `.env.example` es una plantilla segura. Se sube al repositorio porque no contiene secretos reales, solo nombres de variables y ejemplos.

`.gitignore` indica que `.env` y otros archivos `*.env` no deben subirse. Tambien indica que `.env.example` si puede subirse porque sirve como guia.

Cada integrante debe tener su propio `.env` porque cada computadora puede tener distinta password de PostgreSQL o distinta configuracion local.

Advertencia: nunca subir el archivo `.env` al repositorio.

### Crear `.env`

1. Copiar el archivo `.env.example`.
2. Renombrar la copia como `.env`.
3. Completar los valores reales.

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

`DB_PASSWORD` es la contrasena real del usuario de PostgreSQL.

`JWT_SECRET` es la clave usada para firmar tokens JWT. Debe ser larga y secreta. Si alguien conoce esta clave, podria intentar generar tokens falsos.

`JWT_EXPIRATION_MS` define cuanto dura el token en milisegundos. `86400000` equivale a 24 horas.

`APP_ADMIN_USERNAME`, `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` definen el usuario administrador inicial.

En Spring Boot, una propiedad como `${DB_PASSWORD}` significa: "buscar el valor en una variable de entorno llamada DB_PASSWORD".

El proyecto usa `dotenv-java` para leer `.env` durante desarrollo y cargar esos valores antes de que arranque Spring Boot.

## Configurar `application.properties`

El archivo esta en:

```text
src/main/resources/application.properties
```

La configuracion usa placeholders, no secretos reales:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS}

app.admin.username=${APP_ADMIN_USERNAME}
app.admin.email=${APP_ADMIN_EMAIL}
app.admin.password=${APP_ADMIN_PASSWORD}
```

No escribir contrasenas reales en `application.properties`.

## Ejecutar desde IntelliJ

1. Abrir esta carpeta como proyecto Maven:

```text
C:\Users\Juani Ferrere\Desktop\TPI PROGRAMACIÓN\prode\prode
```

2. Crear el archivo `.env` en la raiz del proyecto.
3. Verificar que PostgreSQL este iniciado.
4. Verificar que exista la base `prode_db`.
5. Abrir la clase principal:

```text
src/main/java/ar/edu/utn/frvm/prode/ProdeApplication.java
```

6. Ejecutar el metodo `main`.
7. Si todo esta correcto, deberian aparecer mensajes similares a:

```text
Tomcat started on port 8080
Started ProdeApplication
```

La API queda disponible en:

```text
http://localhost:8080
```

## Que se implemento en Etapa 2

- Entidad `User`.
- Enum `Role` con valores `USER` y `ADMIN`.
- Registro de usuarios.
- Login de usuarios.
- Generacion de JWT.
- Filtro para autenticar requests con header `Authorization`.
- Endpoint protegido `GET /api/auth/me`.
- Admin inicial precargado usando variables de entorno.
- Manejo basico de errores para Auth.
- Endpoint publico `GET /api/health`.

Todavia no estan implementados equipos, fechas, partidos, pronosticos, rankings, grupos privados ni frontend.

## Explicacion simple

JWT es un token firmado que el cliente guarda y envia en cada request protegida. El servidor valida la firma y sabe que usuario esta haciendo la peticion.

BCrypt es un algoritmo para guardar contrasenas de forma segura. La API nunca guarda la contrasena real, solo un hash.

`USER` representa un usuario comun. `ADMIN` representa un administrador. En etapas futuras el ADMIN podra gestionar datos como equipos, fechas y partidos.

## Pruebas en Insomnia

### 1. Health check

```http
GET http://localhost:8080/api/health
```

Respuesta esperada:

```json
{
  "status": "OK",
  "message": "Prode API funcionando correctamente"
}
```

### 2. Registro

```http
POST http://localhost:8080/api/auth/register
```

Body JSON:

```json
{
  "username": "juan",
  "email": "juan@test.com",
  "password": "Password123!"
}
```

Esperado: devuelve token, username, email y role. No devuelve password.

### 3. Login

```http
POST http://localhost:8080/api/auth/login
```

Body JSON:

```json
{
  "usernameOrEmail": "juan",
  "password": "Password123!"
}
```

Esperado: devuelve token.

### 4. Endpoint protegido sin token

```http
GET http://localhost:8080/api/auth/me
```

Esperado: `401 Unauthorized`.

### 5. Endpoint protegido con token

```http
GET http://localhost:8080/api/auth/me
```

Header:

```text
Authorization: Bearer PEGAR_TOKEN_AQUI
```

Esperado: devuelve el usuario autenticado.

### 6. Login admin precargado

```http
POST http://localhost:8080/api/auth/login
```

Body JSON:

```json
{
  "usernameOrEmail": "admin",
  "password": "USAR_EL_VALOR_DE_APP_ADMIN_PASSWORD_DEL_ENV"
}
```

Esperado: devuelve token con role `ADMIN`.

## Errores comunes

`database "prode_db" does not exist`

Causa: la base no fue creada.

Solucion: crear `prode_db` en pgAdmin.

`password authentication failed for user "postgres"`

Causa: `DB_PASSWORD` esta mal.

Solucion: revisar la contrasena real de PostgreSQL en `.env`.

`Could not resolve placeholder 'DB_PASSWORD'`

Causa: no existe la variable y no se cargo `.env`.

Solucion: crear `.env` en la raiz del proyecto o configurar la variable en IntelliJ.

`Connection refused`

Causa: PostgreSQL esta apagado o el puerto no es `5432`.

Solucion: iniciar PostgreSQL y verificar el puerto.

Error relacionado con `JWT_SECRET`

Causa: falta configurar `JWT_SECRET` o la clave es demasiado corta.

Solucion: agregar `JWT_SECRET` en `.env` con una clave larga.

## Estado actual del desarrollo

Etapa 0, Etapa 1 y Etapa 2 preparadas.

El backend ya puede registrar usuarios, iniciar sesion, devolver JWT y proteger endpoints usando token. La configuracion sensible ahora se maneja con variables de entorno.

## Proximas etapas

- Implementar equipos.
- Implementar fechas.
- Implementar partidos.
- Implementar pronosticos.
- Implementar carga de resultados.
- Implementar calculo de puntos.
- Implementar ranking global y ranking por grupo.
