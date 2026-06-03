# Prode

API REST backend para una plataforma de pronósticos deportivos tipo Prode.

El objetivo final del sistema es permitir usuarios, roles, partidos, pronósticos, resultados, rankings y grupos privados. Actualmente el proyecto llega hasta Etapa 2: autenticación base con usuarios, roles y JWT.

## Integrantes

- Ferrere, Juan Ignacio.
- Pereyra, Benjamín.
- Rochietti, Geremías.
- Acosta, Mateo.

## Tecnologías usadas

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

## Requisitos para ejecutar

- JDK 21 instalado.
- IntelliJ IDEA.
- PostgreSQL instalado y en ejecución.
- pgAdmin4 para administrar PostgreSQL.
- Base de datos `prode_db` creada.
- Maven Wrapper incluido en el proyecto.

## Crear la base PostgreSQL `prode_db`

1. Abrir pgAdmin4.
2. Conectarse al servidor local de PostgreSQL.
3. Hacer click derecho en `Databases`.
4. Seleccionar `Create` y luego `Database`.
5. En `Database`, escribir `prode_db`.
6. En `Owner`, dejar `postgres`.
7. Guardar.

## Configurar `application.properties`

El archivo está en:

```text
src/main/resources/application.properties
```

Ejemplo de configuración:

```properties
spring.application.name=prode

spring.datasource.url=jdbc:postgresql://localhost:5432/prode_db
spring.datasource.username=postgres
spring.datasource.password=MI_PASSWORD_REAL
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080

app.jwt.secret=CAMBIAR_ESTA_CLAVE_SECRETA_LARGA_PARA_DESARROLLO
app.jwt.expiration-ms=86400000
```

`app.jwt.secret` firma el token. Si alguien cambia el token sin conocer esta clave, la firma deja de ser válida.

`app.jwt.expiration-ms` define cuánto dura el token. `86400000` equivale a 24 horas.

En producción, `app.jwt.secret` no debería estar escrito en el archivo: debería venir desde una variable de entorno.

## Ejecutar desde IntelliJ

1. Abrir esta carpeta como proyecto Maven:

```text
C:\Users\Juani Ferrere\Desktop\TPI PROGRAMACIÓN\prode\prode
```

2. Esperar a que IntelliJ descargue las dependencias.
3. Abrir la clase principal:

```text
src/main/java/ar/edu/utn/frvm/prode/ProdeApplication.java
```

4. Ejecutar el método `main`.
5. Si PostgreSQL está encendido y la contraseña es correcta, la API queda disponible en:

```text
http://localhost:8080
```

## Qué se implementó en Etapa 2

- Entidad `User`.
- Enum `Role` con valores `USER` y `ADMIN`.
- Registro de usuarios.
- Login de usuarios.
- Generación de JWT.
- Filtro para autenticar requests con header `Authorization`.
- Endpoint protegido `GET /api/auth/me`.
- Admin inicial precargado.
- Manejo básico de errores para Auth.
- Endpoint público `GET /api/health`.

Todavía no están implementados equipos, fechas, partidos, pronósticos, rankings, grupos privados ni frontend.

## Explicación simple

JWT es un token firmado que el cliente guarda y envía en cada request protegida. El servidor valida la firma y sabe qué usuario está haciendo la petición.

BCrypt es un algoritmo para guardar contraseñas de forma segura. La API nunca guarda la contraseña real, solo un hash.

`USER` representa un usuario común. `ADMIN` representa un administrador. En etapas futuras el ADMIN podrá gestionar datos como equipos, fechas y partidos.

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
  "password": "Admin1234!"
}
```

Esperado: devuelve token con role `ADMIN`.

## Estado actual del desarrollo

Etapa 0, Etapa 1 y Etapa 2 preparadas.

El backend ya puede registrar usuarios, iniciar sesión, devolver JWT y proteger endpoints usando token.

## Próximas etapas

- Implementar equipos.
- Implementar fechas.
- Implementar partidos.
- Implementar pronósticos.
- Implementar carga de resultados.
- Implementar cálculo de puntos.
- Implementar ranking global y ranking por grupo.
