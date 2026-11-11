# Plan de pruebas — Fases 1 a 9 (Android ↔ Railway)

Backend: `https://mdeps-back-end-production-1d49.up.railway.app/`

Marca cada ítem **OK** / **FALLA** / **N/A** y anota captura o tabla MySQL.

---

## ¿Dónde compruebo: Railway, Swagger o la app?

Son **tres lugares distintos**. Para la entrega al profesor lo importante es **app + MySQL en Railway**. Swagger es ayuda extra para ti o el equipo de backend.

| Lugar | Para qué sirve | ¿Lo necesitas en cada fase? |
|-------|----------------|----------------------------|
| **App Android** | Prueba real: login, crear proyecto, Kanban, etc. Si la UI funciona y ves textos como *Synced with server*, el front **sí** está llamando al API. | **Sí — siempre (paso 1)** |
| **Railway → base de datos MySQL** | Ver que los datos **quedaron guardados** en tablas (`users`, `projects`, …). Es la evidencia de “no es solo pantalla local”. | **Sí — después de cada acción que crea/edita** |
| **Swagger (navegador)** | Probar endpoints **sin** la app (GET con Bearer, ver JSON). Útil si algo falla y quieres saber si es back o front. | **Opcional** |

### Cómo abrir MySQL en Railway (dashboard)

1. Entra a [railway.app](https://railway.app) → tu **proyecto** del backend.
2. Haz clic en el servicio **MySQL** (icono de base de datos), **no** en el servicio del API/Java.
3. Pestaña **Data** (o **Connect** / **Variables** según la UI de Railway):
   - Si Railway muestra **Query** o **Tables**: abre una tabla y refresca tras usar la app.
   - Si no hay editor visual: copia `MYSQL_URL` / host, usuario y contraseña y conéctate con **MySQL Workbench**, **DBeaver** o **HeidiSQL** y ejecuta, por ejemplo:
     ```sql
     SELECT * FROM users ORDER BY id DESC LIMIT 5;
     SELECT * FROM projects ORDER BY id DESC LIMIT 5;
     ```
4. El servicio **backend** en Railway sirve sobre todo para **logs** (ver errores 500) y que el deploy esté **Active** — no es donde ves filas de tablas.

### Cómo abrir Swagger (opcional)

- UI: `https://mdeps-back-end-production-1d49.up.railway.app/swagger/index.html`
- Contrato JSON: `https://mdeps-back-end-production-1d49.up.railway.app/swagger/v1/swagger.json`

Pasos típicos en Swagger:

1. En la app: **Sign in** (necesitas el token).
2. En Swagger, busca un GET (ej. `GET /api/v1/projects`).
3. Pulsa **Authorize** (candado) y pega: `Bearer TU_TOKEN_AQUI` (el mismo que guarda la app tras login).
4. **Try it out** → **Execute**. Si ves JSON con tus proyectos, el backend está bien; si la app no los muestra, el problema está en Android.

**Resumen:** usa la **app** para demostrar integración; usa **MySQL en Railway** para capturas de evidencia; usa **Swagger** solo para depurar o mostrar el API al profesor sin el teléfono.

---

## Mapa rápido: acción en la app → tabla en Railway (Data)

En Railway: **MySQL → pestaña Database → Data**. Haz clic en el nombre de la tabla para ver filas. Después de cada acción en el teléfono, pulsa **refresh** en el navegador de tablas (o vuelve a entrar en la tabla).

Convención de esta guía:

| Columna | Significado |
|---------|-------------|
| **Acción en Android** | Qué tocas / qué haces |
| **Tabla(s)** | Nombre exacto como en tu captura de Railway |
| **Qué mirar** | Columna o dato que debe cambiar |
| **Lectura vs escritura** | **Escribe** = deberías ver fila nueva o valor editado; **Lee** = la app trae datos del servidor (la tabla puede ya tener filas del seed del backend) |

### Fase 0 — Login / registro

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| **Sign up** (usuario nuevo) | `users` | Nueva fila: `username`, `email`, rol | Escribe |
| Sign up | `profiles` | Fila asociada al mismo usuario (nombre, email) | Escribe (si el back la crea al registrar) |
| **Sign in** | `login_attempts` | Puede aparecer intento exitoso (si el back lo registra) | Escribe (opcional) |
| Sign in | — | No hay tabla: el token va en el teléfono (DataStore) | N/A |

```sql
SELECT id, username, email, role FROM users ORDER BY id DESC LIMIT 5;
```

### Fase 1 — Perfil

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Abrir **Profile** | `profiles`, `users` | Mismo nombre/email/rol que en la app | Lee |
| KPIs del perfil | `profile_stats` | Fila con tu `userId`: proyectos, on track, etc. | Lee |
| **Guardar correo** (Account & security) | `users` | Columna **`email`** = el nuevo correo | Escribe |
| Guardar correo | `profiles` | **`email`** alineado (el API actualiza ambos) | Escribe |

```sql
SELECT id, email, role FROM users WHERE username = 'TU_USUARIO';
SELECT id, email, name FROM profiles WHERE email LIKE '%TU_DOMINIO%';
SELECT * FROM profile_stats WHERE userId = TU_ID_NUMERICO LIMIT 1;
```

### Fase 2 — Proyectos

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Home / **Projects** (abrir o refresh) | `projects` | Misma lista que en la app (título, estado) | Lee |
| **Crear proyecto** | `projects` | Nueva fila: `name`/`title`, estado, `userId` tuyo | Escribe |
| Editar detalle del proyecto | `projects` | Campo que cambiaste (estado, descripción, etc.) | Escribe |

```sql
SELECT id, name, status, userId FROM projects ORDER BY id DESC LIMIT 10;
```

### Fase 3 — Kanban / tareas

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Abrir Kanban de un proyecto | `boards` | Tablero(s) ligados al `projectId` | Lee |
| Abrir Kanban | `collaboration_tasks` | Tareas con `boardId` del proyecto | Lee |
| **Crear tarjeta** | `collaboration_tasks` | Nueva fila: título, columna/estado | Escribe |
| Mover / editar tarjeta | `collaboration_tasks` | `status` / columna actualizada | Escribe |
| Eliminar tarjeta | `collaboration_tasks` | La fila desaparece (o queda inactiva según back) | Escribe |

```sql
SELECT id, boardId, title, status FROM collaboration_tasks ORDER BY id DESC LIMIT 10;
```

### Fase 4 — Analytics y Governance

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Pestaña **Analytics** | `analytics` | KPIs / métricas que el back expone | Lee |
| Analytics refresh | `profile_stats` o `stats` vía API | Coherente con portafolio (si hay filas) | Lee |
| **Governance** (abrir / refresh) | `compliance_metrics` | % compliance que ves en pantalla | Lee |
| Governance heatmap | `risk_matrices` | Datos agregados de la matriz | Lee |
| Governance lista de riesgos | `risks` | Filas que aparecen en la app (proyecto, severidad) | Lee |

**Importante:** crear riesgos nuevos desde la app suele quedarse en **Room local**; Governance **lee** `risks` del servidor al sincronizar. Para probar: que en `risks` ya haya filas en MySQL y al hacer pull-to-refresh en Governance veas los mismos títulos.

```sql
SELECT * FROM compliance_metrics LIMIT 5;
SELECT id, projectId, title, severity FROM risks ORDER BY id DESC LIMIT 10;
SELECT * FROM risk_matrices LIMIT 5;
SELECT * FROM analytics LIMIT 5;
```

### Fase 5 — Preferencias (perfil)

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Tema / idioma / notificaciones + **Guardar cambios del perfil** | `user_settings` | `theme`, `language`, `pushNotifications` / `emailNotifications` | Escribe |

**API solo tiene GET + PUT (sin POST).** Si la tabla está vacía, `PUT` devuelve **404** hasta que exista una fila:

1. En Railway → `user_settings` → **+ Row** (o pide al backend crear la fila en **sign-up**).
2. Rellena al menos: `theme` (`light`/`dark`), `language` (`es`/`en`), `timezone` (`Europe/Madrid`), notificaciones en `true`/`false`.
3. En la app → Perfil → cambia tema/idioma → **Guardar cambios del perfil**.
4. Refresca la tabla: deberían actualizarse las columnas.

```sql
SELECT * FROM user_settings LIMIT 5;
```

### Fase 6 — Agenda (Home)

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| **Sync agenda** | `meetings` | Reuniones listadas en Home | Lee |
| Sync agenda | `user_schedule_entries` o `schedules` | Entradas de agenda (nombre puede variar en Railway) | Lee |
| **Save check-in** | `user_schedule_entries` (o similar) | Nueva fila con fecha/nota de check-in | Escribe |
| **Schedule meeting / Reunión API** | `meetings` | Nueva fila: título, fecha | Escribe |

```sql
SELECT * FROM meetings ORDER BY id DESC LIMIT 5;
```

(Si tu tabla se llama distinto, busca en Data la que empiece por `user_schedule`.)

### Fase 7 — Informes y capacidad

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Analytics → **Export** / informes | `reports` | Informes que lista la sección *Executive reports (Railway)* | Lee |
| Analytics refresh | `resource_plannings` | Filas por departamento / capacidad | Lee |
| Tras login | `users` + `profile_stats` | `userId` correcto en stats | Lee |

```sql
SELECT * FROM reports ORDER BY id DESC LIMIT 5;
SELECT * FROM resource_plannings LIMIT 10;
```

### Fase 8 — Soporte

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Centro de soporte (abrir) | `support_tickets`, `chats` | Listas (vacías OK) | Lee |
| **New ticket** | `support_tickets` | Nueva fila: asunto, estado | Escribe |
| **New chat** | `chats` | Nueva fila | Escribe |
| New chat | `chat_users` | Posible enlace usuario ↔ chat | Escribe (según back) |

```sql
SELECT * FROM support_tickets ORDER BY id DESC LIMIT 5;
SELECT * FROM chats ORDER BY id DESC LIMIT 5;
```

### Fase 9 — Mensajes

| Acción en Android | Tabla(s) | Qué mirar | Tipo |
|-------------------|----------|-----------|------|
| Abrir un chat | `chat_messages` | Mensajes previos del `chatId` | Lee |
| **Send message** | `chat_messages` | Nueva fila: texto, `chatId`, timestamp | Escribe |

```sql
SELECT * FROM chat_messages ORDER BY id DESC LIMIT 10;
```

### Tablas que ves en Railway pero la app no escribe (solo contexto)

| Tabla | Nota |
|-------|------|
| `__EFMigrationsHistory` | Migraciones del backend — ignorar en la demo |
| `departments`, `workspace_selections`, `brandings` | Datos del back; no es flujo principal de la app |
| `profile_skills`, `endorsements`, `chat_insights` | No integrados en esta versión Android |
| `dashboard_tasks` | Distinto de Kanban (`collaboration_tasks`) |
| `subscriptions`, `admin_policies`, `system_settings` | Admin / SaaS — no usados en la app |

### Mini rutina de evidencia (capturas para el profesor)

1. `users` — tu usuario tras sign up / cambio de email  
2. `projects` — tras crear iniciativa  
3. `collaboration_tasks` — tras tarjeta Kanban  
4. `risks` + `compliance_metrics` — tras abrir Governance  
5. `meetings` o `user_schedule_*` — tras agenda  
6. `support_tickets` + `chat_messages` — tras soporte  

---

## Fase 0 — Sesión y red

| # | Acción | ¿Conectado al backend? | Cómo verificar |
|---|--------|------------------------|----------------|
| 0.1 | **Sign up** con usuario nuevo | Sí | Usuario en MySQL `users`; sin error en app |
| 0.2 | **Sign in** | Sí | Entras al shell; no solo pantalla local |
| 0.3 | Token | Sí | Tras login, crear proyecto: si fallara sin token verías error HTTP |
| 0.4 | **401** | Sí | (Opcional) token inválido → vuelve a login + snackbar sesión expirada |

**Nota:** Recuperación de contraseña sigue **simulada local** (no API).

---

## Fase 1 — Perfil API

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 1.1 | Abrir **Perfil** | `GET profiles?email=` | Nombre/email/rol actualizados vs registro |
| 1.2 | Pull / entrar a pestaña | `GET stats?userId=` | KPIs con etiqueta *Synced with server* (si hay fila) |
| 1.3 | Editar email y **Guardar** | `PATCH /users/{userId}` | `users.email` (+ `profiles.email`) en Railway |

---

## Fase 2a — Proyectos lectura

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 2a.1 | **Home** o **Projects** al abrir | `GET /projects` | Lista coincide con datos en MySQL `projects` |
| 2a.2 | Pull-to-refresh en Home | `GET /projects` | Nuevos proyectos de otro cliente aparecen |

---

## Fase 2b — Proyectos escritura

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 2b.1 | **Crear proyecto** | `POST /projects` | Nuevo `id` en MySQL; visible en lista |
| 2b.2 | Abrir detalle y guardar cambios | `PUT /projects/{id}` | Campo editado en Railway |

---

## Fase 3a — Boards + tareas lectura

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 3a.1 | Abrir **Kanban** de un proyecto | `GET boards`, `GET tasks?boardId=` | Columnas con tareas del servidor (no solo seed local) |

---

## Fase 3b — Tareas CRUD

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 3b.1 | Crear tarjeta Kanban | `POST /tasks` | Fila en `collaboration_tasks` |
| 3b.2 | Mover / editar tarea | `PUT /tasks/{id}` | Estado actualizado en DB |
| 3b.3 | Eliminar (si UI lo permite) | `DELETE /tasks/{id}` | Desaparece en API y UI |

---

## Fase 4a — Analytics

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 4a.1 | Pestaña **Analytics** | `GET /analytics` | KPIs/capacidad distintos a solo cálculo local; etiqueta verde si hay overlay |
| 4a.2 | Pull-to-refresh | `GET /stats` | Salud de portafolio alineada si hay datos |

---

## Fase 4b — Governance / riesgos

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 4b.1 | **Governance** | `GET /compliance-metrics` | % compliance remoto + etiqueta sincronizado |
| 4b.2 | Misma pantalla | `GET /risks` | Riesgos en matriz de proyecto (Room + API) |
| 4b.3 | Heatmap en Governance | `GET /risk-matrix` | Cuadrícula 5×3 con números del servidor |
| 4b.4 | Pull-to-refresh Governance | Varios | Snackbar solo si error de red |

---

## Fase 5 (Opcional A) — User settings

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 5.1 | Tema oscuro ON/OFF | `PUT /user-settings` | Tras guardar, `user_settings` en MySQL |
| 5.2 | Idioma EN/ES | `PUT` + pull | Locale de app; campo `language` en servidor |
| 5.3 | Notificaciones | `PUT` | `pushNotifications` / `emailNotifications` en `user_settings` |
| 5.4 | Al abrir perfil | `GET /user-settings` | 404 → la app hace seed con valores locales (OK) |

---

## Fase 6 (Opcional B + hardening) — Agenda

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 6.1 | Home → **Sync agenda** | `GET meetings`, `GET user-schedules/user/{id}` | Ítems tipo Meeting/Schedule |
| 6.2 | **Save check-in** | `POST user-schedules` | Nueva fila `user_schedules` |
| 6.3 | **Reunión API** | `POST /meetings` | Fila `meetings` |
| 6.4 | Errores de red | — | Snackbar en Home/Kanban/Analytics/Governance/Perfil |

---

## Fase 7 — Informes + bootstrap

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 7.1 | Tras login (automático) | Perfil + agenda | `userId` numérico tras sync perfil |
| 7.2 | Analytics → **Export** | `GET /reports` | Sección *Executive reports (Railway)* |
| 7.3 | Analytics refresh | `GET /resource-planning` | Filas de capacidad por departamento si API tiene datos |

---

## Fase 8 — Soporte + APK

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 8.1 | Campana Home o Perfil → soporte | `GET support-tickets`, `GET chats` | Listas cargan (vacías OK) |
| 8.2 | **New ticket** | `POST support-tickets` | Tabla `support_tickets` |
| 8.3 | **New chat** | `POST /chats` | Tabla `chats` |
| 8.4 | APK release | — | `gradlew.bat :app:assembleRelease` genera APK instalable |

---

## Fase 9 — Mensajes de chat

| # | Acción | Endpoint | Verificación |
|---|--------|----------|--------------|
| 9.1 | Crear chat (fase 8) si no hay | `POST /chats` | Al menos un chat |
| 9.2 | Tocar chat en lista | `GET /chat-messages?chatId=` | Hilo vacío o con mensajes |
| 9.3 | Escribir y **Send message** | `POST /chat-messages` | Mensaje visible al refrescar; fila en `chat_messages` |

---

## Orden recomendado (una sola sesión ~45 min)

1. Sign up → Sign in  
2. Perfil (sync)  
3. Crear proyecto → Kanban tarea  
4. Analytics + Governance (refresh)  
5. Home agenda (check-in + reunión)  
6. Soporte: ticket + chat + **mensaje**  
7. MySQL: screenshot de `users`, `projects`, `collaboration_tasks`, `risks`, `user_settings`, `user_schedules`, `support_tickets`, `chats`, `chat_messages`

---

## Qué sigue siendo mayormente local (decirlo al profesor)

- **Hitos/milestones** por proyecto (Room; no hay CRUD remoto de milestones en esta app).
- **Políticas de governance** (SOC2 toggles en DataStore).
- **Password recovery** (mock, sin API).
- **Room** como caché offline cuando la API falla (comportamiento esperado).

---

## Checklist rápido “no solo login”

Si puedes marcar **sí** a todo, el front está integrado más allá de sign-in/sign-up:

- [ ] Proyecto creado aparece en MySQL  
- [ ] Tarea Kanban en MySQL  
- [ ] Analytics/Governance muestran *Synced with server* o datos de heatmap  
- [ ] Perfil guarda en API  
- [ ] User-settings o agenda con POST visible en DB  
- [ ] Ticket o mensaje de chat en DB  

Guarda este archivo rellenado como evidencia de entrega.
