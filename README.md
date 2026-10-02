# Hercufy (backend)

Backend de Hercufy: Spring Boot 3.4 + Java 21 + PostgreSQL, construido a partir de la
plantilla [Spring-template](https://github.com/frankxhunter/Spring-template).

Esta fase cubre **auth completo + catalogo de ejercicios + CRUD de rutinas**. No hay
ningun endpoint de IA todavia (eso es la siguiente fase, sin tocar aqui).

> Nota sobre como se ha hecho este proyecto: se ha escrito y revisado con mucho cuidado,
> pero **no se ha podido compilar ni ejecutar** en el entorno donde se generó (sin acceso
> a Maven Central ni a Docker). Se ha comparado línea a línea contra la plantilla
> original para todo lo que se reutiliza sin cambios, y se ha revisado a mano el resto,
> pero conviene que la primera vez que lo compiles vayas con margen por si aparece algún
> error de compilación suelto que aquí no se pudo detectar.

## Poner en marcha

1. Copia `.env.example` a `.env` y rellena al menos `MY_DATABASE_URL` / `MY_USER` /
   `MY_PASSWORD` / `MY_DATABASE` y `JWT_SECRET` (genera este último con, por ejemplo,
   `openssl rand -base64 48`).
2. Elige uno de los dos flujos:
   - **Desarrollando** (recarga más rápida): `docker compose up -d database` para
     tener solo Postgres, y `mvn spring-boot:run` (o desde el IDE) para el backend.
     Aquí `MY_DATABASE_URL` del `.env` debe apuntar a `localhost:5432`, tal como viene
     en `.env.example`.
   - **Backend también en Docker**: `docker compose up --build` levanta los dos
     servicios (`database` y `backend`). El servicio `backend` ya sobreescribe
     `MY_DATABASE_URL` para apuntar a `database:5432` en vez de `localhost` (dentro de
     la red de compose, `localhost` sería el propio contenedor del backend); el resto
     de variables las toma de tu `.env` tal cual. No hace falta que cambies nada a
     mano al pasar de un flujo a otro.
3. Al arrancar por primera vez, la app importa el catálogo de ejercicios (ver más
   abajo) y crea las tablas sola (`ddl-auto=update`; ver el TODO de Flyway en
   `application.properties`).
4. La API queda en `http://localhost:8080/api/...`.

El correo (verificacion de email) y el login con Google pueden dejarse sin configurar:
la app arranca igual y esos dos flujos concretos simplemente no funcionaran del todo
hasta que definas `MAIL_HOST`/etc. o `GOOGLE_CLIENT_ID` (ver mas abajo).

## Que hay

### Auth (`/api/auth`)
Copiado de la plantilla casi sin cambios: registro, login, refresh con rotacion,
logout, verificacion de email por correo, y login con Google. Cambios sobre la
plantilla:
- **Registro pide tambien `username`** (nombre visible), ademas de email y contrasena.
  Se usa `RegisterRequest` en vez de `UserDto` (que se sigue usando solo para el login).
- El envio de correo de confirmacion **nunca bloquea el registro**: si el SMTP no esta
  configurado, el intento de envio falla en silencio y queda avisado en el log, con el
  token de verificacion visible ahi para poder confirmar la cuenta a mano mientras
  desarrollas (ver `EmailService`). En cuanto configures un proveedor real (Gmail,
  SendGrid, Mailtrap...), el envio real empieza a funcionar sin tocar código.
- **Login con Google se mantiene en el backend** tal cual estaba en la plantilla, pero
  el frontend no lo usa todavia (asi lo pediste). Sin `GOOGLE_CLIENT_ID` configurado, el
  endpoint sigue arrancando pero rechazara cualquier token.

### Usuario (`/api/users`)
Nuevo. `GET /me`, `PATCH /me` (cambiar `username`), y **`DELETE /me`** (borrado de
cuenta, pensado para el requisito de las tiendas de apps). Al borrar la cuenta se
eliminan tambien sus tokens y sus rutinas.

### Catalogo de ejercicios (`/api/exercises`)
Importa el dataset completo de **free-exercise-db** (876 ejercicios) al arrancar, desde
un JSON empaquetado en `resources/data` (no depende de internet en tiempo de
ejecucion). Incluye ademas las 55 traducciones al espanol que ya se habian hecho para
el prototipo del frontend; el resto del catalogo se sirve con su nombre en ingles hasta
que se traduzca mas adelante.

- `GET /api/exercises?q=&muscleGroup=&page=&size=`: busqueda paginada. El criterio de
  busqueda (`q`) es el mismo algoritmo que ya usaba el prototipo del frontend (mismo
  normalizado y tokenizado), para que el resultado no cambie al dejar de usar datos
  simulados.
- `GET /api/exercises/muscle-groups`: los 6 grupos (pecho, espalda, hombros, brazos,
  piernas, core) para pintar los filtros, igual que en el frontend.
- `GET /api/exercises/{id}`: detalle completo (musculos, instrucciones, imagenes).

El catalogo se lee una unica vez al arrancar y se guarda en memoria (ver
`ExerciseCatalogService`); no hace falta ir a base de datos en cada busqueda. Limite
conocido: si se edita el catalogo en caliente en el futuro, hara falta refrescar esa
cache (hoy no hay endpoint para eso, no hace falta con un catalogo estatico).

Las imagenes no se sirven desde aqui: se guarda la ruta relativa de cada ejercicio y la
API construye la URL completa contra `EXERCISE_IMAGES_BASE_URL` (por defecto, el propio
repositorio de free-exercise-db en GitHub). **Antes de publicar la aplicacion, verifica
la licencia de ese dataset** y sustituye esta URL por tu propio almacenamiento.

### Rutinas (`/api/plans`)
CRUD completo de rutinas, dias y ejercicios, todo protegido por dueño (cada operacion
comprueba que la rutina pertenece al usuario autenticado; si no, responde 404 en vez de
403, para no filtrar si el recurso existe). Resumen:

```
GET    /api/plans                                          listar mis rutinas
POST   /api/plans                                           crear (nombre + dias)
GET    /api/plans/{id}                                       ver una rutina
PATCH  /api/plans/{id}                                        renombrar / activar-desactivar
DELETE /api/plans/{id}                                        eliminar
GET    /api/plans/active/day?dayOfWeek=1..7                   el dia de la rutina activa para ese dia
POST   /api/plans/{id}/days                                   anadir dia
PATCH  /api/plans/{id}/days/{dayId}                           renombrar dia
DELETE /api/plans/{id}/days/{dayId}                           quitar dia
POST   /api/plans/{id}/days/{dayId}/exercises                 anadir ejercicio al dia
PUT    /api/plans/{id}/days/{dayId}/exercises/{exId}          reemplazar series/reps/peso
DELETE /api/plans/{id}/days/{dayId}/exercises/{exId}          quitar ejercicio
PUT    /api/plans/{id}/days/{dayId}/exercises/reorder         reordenar los ejercicios de un dia
```

Solo puede haber una rutina activa por usuario; activar una desactiva las demas
automaticamente. `GET /active/day` recibe `dayOfWeek` como parametro en vez de
calcularlo el propio servidor, para no depender de la zona horaria del servidor: quien
decide que dia es "hoy" es el cliente.

El modelo de datos sigue el plan tecnico: `TrainingPlan` (la rutina semanal) →
`PlanDay` (un dia) → `PlanExercise` (un ejercicio del dia, con series, repeticiones y
peso opcional). `scheduleType` esta preparado para anadir mas adelante una
programacion por rotacion (A/B/C) sin migrar lo ya guardado.

## Referencia de la API

Todo lo que no es `/api/auth/**` requiere autenticación:
`Authorization: Bearer <accessToken>`. El `accessToken` sale del login/registro/refresh
(ver más abajo) y expira en `JWT_ACCESS_EXPIRATION_MS` (15 minutos por defecto); cuando
caduque, usa `/api/auth/refresh` con el `refreshToken` para conseguir uno nuevo (rota
también el refresh token: guarda siempre el que te devuelva la última llamada).

Los cuerpos de petición son JSON (`Content-Type: application/json`). Las respuestas de
error casi siempre son un texto plano simple con el mensaje (no un JSON estructurado);
la única excepción clara es el límite de peticiones (429), que sí es JSON. Esto es
igual que en la plantilla original, no algo que haya cambiado.

### Contraseñas

Toda contraseña (registro y login) debe tener entre 8 y 20 caracteres, e incluir al
menos: una mayúscula, una minúscula, un número y un símbolo, sin espacios. Ejemplo
válido: `Passw0rd!`.

### Autenticación — `/api/auth` (sin token)

**`POST /api/auth/register`**
```json
// petición
{ "username": "Frank", "email": "frank@example.com", "password": "Passw0rd!" }
```
- `username`: 2 a 40 caracteres. `email`: formato válido, 8 a 40 caracteres.
- `201 Created`, cuerpo: texto "User registered. Please check your email to confirm your account."
- `409 Conflict` si ya existe una cuenta con ese email y ya está verificada.
- Si existe pero *no* está verificada, este mismo endpoint actualiza username/contraseña
  y reenvía la verificación (permite "reintentar" un registro a medias).
- Dispara el correo de confirmación (ver más abajo si no tienes SMTP configurado).

**`POST /api/auth/login`**
```json
// petición
{ "email": "frank@example.com", "password": "Passw0rd!" }
```
- `200 OK`, header `Authorization: Bearer <accessToken>`, cuerpo:
```json
{
  "token": "…",
  "refreshToken": "…",
  "tokenType": "Bearer",
  "expiresInMs": 900000,
  "refreshExpiresInMs": 2592000000,
  "email": "frank@example.com"
}
```
- `403 Forbidden` si el email o la contraseña no son correctos, o si el email todavía
  no está verificado (el login se bloquea hasta confirmar el correo).

**`GET /api/auth/login`** — comprobación rápida de sesión. Si mandas un `Authorization`
válido, `200 OK` con tu email en texto plano; si no, `403 Forbidden`. Pensado más para
depurar que para que lo use el frontend.

**`POST /api/auth/google`**
```json
{ "token": "<id_token de Google>" }
```
Mismo formato de respuesta que `/login`. Hoy no puede funcionar de verdad sin
`GOOGLE_CLIENT_ID` configurado (ver README general); lo dejamos listo para cuando haga
falta, el frontend no lo llama todavía.

**`POST /api/auth/refresh`**
```json
{ "refreshToken": "…" }
```
- `200 OK`, mismo formato de respuesta que `/login` (con un `refreshToken` nuevo: el
  anterior queda revocado, no lo reutilices).
- `401 Unauthorized` si el refresh token es inválido, caducado, revocado o no es del
  usuario que dice ser.

**`GET /api/auth/confirm-email?token=…`** — se llama desde el enlace del correo.
`200 OK` (texto) si confirma bien; `400 Bad Request` si el token es inválido o caducado.

**`POST /api/auth/logout`**
```json
{ "refreshToken": "…" }
```
Revoca ese refresh token. `200 OK` siempre (incluso si el token ya no era válido:
pensado para que cerrar sesión nunca falle en el cliente). El *access* token no se
puede revocar (es sin estado): el cliente debe simplemente descartarlo.

### Usuario — `/api/users` (con token)

**`GET /api/users/me`** → `200 OK`:
```json
{ "id": 1, "username": "Frank", "email": "frank@example.com", "role": "USER", "emailVerified": true }
```

**`PATCH /api/users/me`**
```json
{ "username": "Frank M." }
```
→ `200 OK` con el mismo formato que `GET /me`. Solo cambia el username; el email no es
editable desde aquí.

**`DELETE /api/users/me`** → `204 No Content`. Borra la cuenta, sus tokens y todas sus
rutinas. No hay confirmación en dos pasos a nivel de API: si el frontend lo expone, que
pida confirmación él antes de llamar.

### Catálogo de ejercicios — `/api/exercises` (con token)

**`GET /api/exercises?q=&muscleGroup=&page=&size=`**
- Todos los parámetros son opcionales. `muscleGroup` es una de las claves de
  `/muscle-groups` (`pecho`, `espalda`, `hombros`, `brazos`, `piernas`, `core`).
  `page` empieza en 0; `size` por defecto 20.
- `200 OK`, formato estándar de página de Spring:
```json
{
  "content": [
    { "id": "Barbell_Bench_Press_-_Medium_Grip", "name": "Barbell Bench Press - Medium Grip",
      "nameEs": "Press banca con barra", "translated": true, "level": "intermediate",
      "equipment": "barbell", "category": "strength", "primaryMuscle": "chest",
      "muscleGroup": "pecho", "imageUrls": ["https://…/0.jpg", "https://…/1.jpg"] }
  ],
  "totalElements": 3, "totalPages": 1, "size": 20, "number": 0,
  "first": true, "last": true, "empty": false
}
```

**`GET /api/exercises/muscle-groups`** → `200 OK`, los 6 grupos siempre en este orden:
```json
[
  { "key": "pecho", "label": "Pecho" },
  { "key": "espalda", "label": "Espalda" },
  { "key": "hombros", "label": "Hombros" },
  { "key": "brazos", "label": "Brazos" },
  { "key": "piernas", "label": "Piernas" },
  { "key": "core", "label": "Core" }
]
```

**`GET /api/exercises/{id}`** → `200 OK`:
```json
{
  "id": "Barbell_Bench_Press_-_Medium_Grip", "name": "Barbell Bench Press - Medium Grip",
  "nameEs": "Press banca con barra", "translated": true, "level": "intermediate",
  "force": "push", "mechanic": "compound", "equipment": "barbell", "category": "strength",
  "primaryMuscles": ["chest"], "secondaryMuscles": ["shoulders", "triceps"],
  "instructions": ["Lie back on a flat bench…", "…"],
  "imageUrls": ["https://…/0.jpg", "https://…/1.jpg"]
}
```
`404 Not Found` si el id no existe. `translated: false` significa que `nameEs` es en
realidad el nombre en inglés (todavía no hay traducción para ese ejercicio).

### Rutinas — `/api/plans` (con token)

Todo lo que no sea `GET` sobre un `planId`/`dayId` ajeno da `404 Not Found` (nunca 403,
para no confirmar si el recurso existe).

**`GET /api/plans`** → `200 OK`, lista de rutinas (ver forma de `TrainingPlanResponse`
más abajo).

**`POST /api/plans`**
```json
{
  "name": "Volumen de otoño",
  "days": [
    { "name": "Pecho, hombro y tríceps", "dayOfWeek": 1 },
    { "name": "Pierna", "dayOfWeek": 4 }
  ]
}
```
- `dayOfWeek`: 1 (lunes) a 7 (domingo). Los días se crean sin ejercicios; se añaden
  después con el endpoint de ejercicios. La primera rutina que crea un usuario se
  activa sola; las siguientes se crean inactivas.
- `201 Created`, cuerpo: la rutina creada (`TrainingPlanResponse`, forma completa más
  abajo).

**`GET /api/plans/{planId}`** → `200 OK`, `TrainingPlanResponse`.

**`PATCH /api/plans/{planId}`** — ambos campos opcionales, solo se aplica lo que envíes:
```json
{ "name": "Nuevo nombre", "active": true }
```
Poner `"active": true` desactiva automáticamente cualquier otra rutina activa del
usuario (solo puede haber una a la vez). → `200 OK`, `TrainingPlanResponse`.

**`DELETE /api/plans/{planId}`** → `204 No Content`.

**`GET /api/plans/active/day?dayOfWeek=1..7`** → `200 OK`, `PlanDayResponse` (el día de
la rutina activa para ese día de la semana). `404 Not Found` si no hay rutina activa o
si esa rutina no entrena ese día. **El "hoy" lo decide el cliente**: el backend no mira
su propio reloj, para no depender de en qué zona horaria esté desplegado.

**`POST /api/plans/{planId}/days`**
```json
{ "name": "Torso completo", "dayOfWeek": 5 }
```
→ `201 Created`, devuelve **la rutina entera actualizada** (`TrainingPlanResponse`), no
solo el día nuevo.

**`PATCH /api/plans/{planId}/days/{dayId}`**
```json
{ "name": "Nuevo nombre del día" }
```
→ `200 OK`, rutina entera actualizada.

**`DELETE /api/plans/{planId}/days/{dayId}`** → `200 OK`, rutina entera actualizada (no
`204`: se hizo así a propósito para no obligar a otra llamada aparte para refrescar la
vista).

**`POST /api/plans/{planId}/days/{dayId}/exercises`**
```json
{ "exerciseId": "Barbell_Bench_Press_-_Medium_Grip", "sets": 4, "reps": 8, "weightKg": 60 }
```
- `exerciseId`: debe existir en el catálogo (`404 Not Found` si no). `sets`: 1 a 20.
  `reps`: 1 a 200. `weightKg`: opcional (omítelo o pon `null` para un ejercicio sin
  peso), si se manda no puede ser negativo.
- `201 Created`, rutina entera actualizada.

**`PUT /api/plans/{planId}/days/{dayId}/exercises/{exerciseEntryId}`** — reemplaza
series, repeticiones y peso **a la vez** (no es un PATCH parcial: manda los tres
siempre, igual que hace la pantalla de edición del frontend):
```json
{ "sets": 4, "reps": 6, "weightKg": 65 }
```
→ `200 OK`, rutina entera actualizada. Nota: `exerciseEntryId` es el `id` de la fila
dentro del día (el campo `id` de `PlanExerciseResponse`), **no** el `exerciseId` del
catálogo.

**`DELETE /api/plans/{planId}/days/{dayId}/exercises/{exerciseEntryId}`** → `200 OK`,
rutina entera actualizada.

**`PUT /api/plans/{planId}/days/{dayId}/exercises/reorder`**
```json
{ "orderedExerciseIds": ["<id1>", "<id2>", "<id3>"] }
```
Debe incluir exactamente los mismos `id` que ya tiene ese día (ni de más ni de menos),
en el orden nuevo deseado; si no, `400 Bad Request`. → `200 OK`, rutina entera
actualizada.

#### Forma de una rutina completa (`TrainingPlanResponse`)

Es lo que devuelven casi todos los endpoints de `/api/plans` (la rutina entera, con sus
días y los ejercicios de cada día ya resueltos contra el catálogo):

```json
{
  "id": "b6b8da4a-4b04-49c8-8a91-4ad7b473c4ac",
  "name": "Volumen de otoño",
  "active": true,
  "scheduleType": "WEEKDAY",
  "days": [
    {
      "id": "d1c1a2b3-…",
      "name": "Pecho, hombro y tríceps",
      "position": 0,
      "dayOfWeek": 1,
      "exercises": [
        {
          "id": "e1f1a2b3-…",
          "exerciseId": "Barbell_Bench_Press_-_Medium_Grip",
          "exerciseName": "Barbell Bench Press - Medium Grip",
          "exerciseNameEs": "Press banca con barra",
          "position": 0,
          "sets": 4,
          "reps": 8,
          "weightKg": 60
        }
      ]
    }
  ],
  "createdAt": "2026-09-27T10:00:00Z",
  "updatedAt": "2026-09-27T10:05:00Z"
}
```

### Errores más comunes

| Código | Cuándo | Cuerpo |
|---|---|---|
| 400 | Validación fallida (campo obligatorio, formato, límites) | Texto: `Field: <campo> - <mensaje>;` por cada campo |
| 401 | Refresh token inválido/caducado/revocado | Texto con el motivo |
| 403 | Login incorrecto, email sin verificar, token de Google inválido | Texto con el motivo |
| 404 | Recurso no encontrado o no es tuyo (rutina, día, ejercicio, id de catálogo) | Texto con el motivo |
| 409 | Email ya registrado | Texto: "Email: An account with this email already exists" |
| 429 | Límite de peticiones superado (100/min por IP, para toda la API) | JSON: `{"error": "Too many requests. Please try again later."}` |
| 500 | Error inesperado | Texto: "An error ocurred: …" |

## Decisiones y limitaciones de esta fase

- **`ddl-auto=update`, sin Flyway todavia.** Es lo mismo que ya usaba la plantilla;
  antes de produccion conviene migrar a Flyway (esta anotado con un TODO en
  `application.properties`).
- **`spring.jpa.open-in-view=false`**: los servicios devuelven DTOs, no entidades, así
  que no hace falta mantener la sesión de Hibernate abierta durante el renderizado
  JSON. Si en el futuro algún endpoint nuevo devuelve una entidad directamente en vez
  de un DTO, tenlo en cuenta (puede fallar al intentar cargar una relación perezosa
  fuera de la transacción).
- **Sin cache de escritura para el catalogo**: pensado para un catalogo que no cambia
  en caliente. Si mas adelante hay un panel de administracion que edite ejercicios,
  hay que anadir una forma de refrescar `ExerciseCatalogService`.
- **Rate limiting** sigue siendo el global de la plantilla (100 peticiones/minuto por
  IP, para toda la API). Cuando se añadan los endpoints de IA convendrá un límite más
  estricto y por usuario solo para esas rutas (ya anotado en `RateLimitingFilter`).
- **Login con email sin verificar**: la plantilla original no tenía un manejador
  específico para este caso (Spring Security lo señala con `DisabledException` antes de
  comprobar la contraseña) y acababa devolviendo un 500 genérico. Se añadió un
  `@ExceptionHandler` para que dé un 403 claro (ver tabla de errores más abajo).
- **`docker-compose.yml`** levanta la base de datos y, si quieres, el backend
  empaquetado (`docker compose up --build`). El `dockerfile` de producción con nginx
  delante se retoma en la fase de despliegue.

## Conectar el frontend

El prototipo de Angular/Ionic hoy usa datos simulados detrás de dos contratos
(`PlanRepository` y `AssistantPort`, en `app.config.ts`). Cuando toque conectar esta
API, hay que:

1. Añadir un `HttpPlanRepository` que llame a `/api/plans/...` con estas mismas formas.
2. Añadir un servicio de autenticación real contra `/api/auth` y `/api/users/me`,
   guardando el access token y usando el refresh token para renovarlo.
3. Apuntar `ExerciseService` del frontend a `/api/exercises` en vez de al catálogo
   embebido (los parámetros `q` y `muscleGroup`, y la forma de los grupos musculares,
   ya están pensados para que coincidan).

`AssistantPort` (Hercules) se queda con su implementación simulada hasta la siguiente
fase, que sí tocará IA.
