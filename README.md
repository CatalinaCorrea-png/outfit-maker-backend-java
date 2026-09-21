# Outfit Maker — Backend

API REST para gestionar un placard virtual: prendas, outfits y autenticación con JWT.

**Stack:** Java 21 · Spring Boot 4 · Spring Security 7 · Spring Data JPA · PostgreSQL · JJWT · Gradle

## Requisitos

- JDK 21
- Docker (para PostgreSQL)

## Cómo levantarlo

1. Levantar la base de datos:

   ```bash
   docker compose up -d
   ```

   - PostgreSQL en `localhost:5433` (base `outfitmaker_sql`, usuario y contraseña `postgres`)
   - pgAdmin en http://localhost:5050 (`admin@admin.com` / `admin`). Desde pgAdmin el host es `db:5432`, no `localhost:5433`.

2. Definir la variable de entorno `JWT_KEY` con **32 caracteres o más**. Sin ella la app no arranca.

3. Correr la app:

   ```bash
   ./gradlew bootRun
   ```

   Queda escuchando en http://localhost:8080.

Al arrancar, `ProjectBootstrap` carga datos de ejemplo. Usuarios de prueba: `cher@gmail.com` y `dionne@gmail.com`, ambos con contraseña `123`.

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `JWT_KEY` | — (obligatoria) | Clave de firma HS256, mínimo 32 caracteres |
| `JWT_ACCESS_EXPIRATION` | `900000` (15 min) | Vida del access token, en ms |
| `JWT_REFRESH_EXPIRATION` | `604800000` (7 días) | Vida del refresh token, en ms |
| `DB_URL` | `jdbc:postgresql://localhost:5433/outfitmaker_sql` | URL JDBC |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` | Credenciales de la base |
| `JPA_DDL_AUTO` | `create-drop` | En la nube conviene `update` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Orígenes del front, separados por coma |

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/auth/register` | No | Registro de usuario |
| `POST` | `/auth` | No | Login. Devuelve el access token y setea el refresh token en una cookie `httpOnly` |
| `POST` | `/auth/refresh` | Cookie | Rota el refresh token y devuelve un access token nuevo |
| `GET` | `/garments/filtered-garments` | Bearer | Prendas paginadas y filtradas |
| `GET` | `/outfits/filtered-outfits` | Bearer | Outfits paginados |
| `GET` | `/actuator/health` | No | Estado de la app |

Los endpoints protegidos esperan el header `Authorization: Bearer <access token>`.

**Filtros de prendas** (query params, todos opcionales): `userId`, `category`, `name`, `brand`, `pattern`, `formality`, `season`, `active`, `page` (default `0`), `pageSize` (default `6`), `sortBy` (`createdAt` o `name`), `ascending` (default `true`). Sin `active`, solo se listan prendas activas.

## Errores

Todos los errores responden con el mismo formato. El front debe usar `code` para traducir el mensaje; `detail` es para depuración.

```json
{
  "status": 404,
  "code": "USER_NOT_FOUND",
  "error": "Recurso no encontrado",
  "detail": "No se encuentra un usuario registrado con este email: x@y.com",
  "timestamp": "2026-09-21T14:32:11-03:00"
}
```

## Tests

```bash
./gradlew test
```

Corren con el perfil `test` sobre H2 en memoria: no necesitan Docker ni `JWT_KEY`.

## Estructura

```
src/main/java/ar/outfitmaker/
├── bootstrap/       datos de ejemplo
├── config/          seguridad, JWT, CORS
├── controller/      endpoints REST
├── domain/          entidades JPA y enums
├── dto/             records de entrada y salida
├── errors/          excepciones y handler global
├── repository/      repositorios de Spring Data
├── service/         lógica de negocio
└── specification/   filtros dinámicos (JPA Criteria)
```
