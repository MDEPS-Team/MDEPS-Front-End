# Guía de demo — Vantage PMO (Android + Railway)

## Antes de clase

1. Emulador o dispositivo con **internet**.
2. Backend activo: `https://mdeps-back-end-production-1d49.up.railway.app/`
3. Compilar: `gradlew.bat :app:assembleDebug`

## Cuenta sugerida

- Registra un usuario nuevo (**Sign up**) con email corporativo `@` válido, o usa una cuenta ya creada en MySQL.
- Tras login, la app hace **bootstrap**: perfil → resuelve `userId` → agenda y settings.

## Guión (10 min)

| Min | Acción | Qué demostrar |
|-----|--------|----------------|
| 0–1 | Login / registro | Token Bearer, sesión en DataStore |
| 1–2 | **Home** pull-to-refresh | Proyectos desde API; agenda `meetings` + `user-schedules` |
| 2–3 | **Guardar check-in** / **Reunión API** | POST en Railway; volver a sincronizar agenda |
| 3–4 | Crear **proyecto** | POST `projects`; aparece en lista y Home |
| 4–5 | **Kanban** | CRUD tareas contra API |
| 5–6 | **Analytics** | Overlay `analytics` + `resource-planning` + `stats`; exportar → informes `GET reports` |
| 6–7 | **Governance** | Compliance, riesgos, heatmap `risk-matrix` |
| 7–8 | **Perfil** | PATCH profile, stats, user-settings (tema/idioma/notificaciones) |
| 8–10 | **MySQL Railway** | Filas en `users`, `projects`, `collaboration_tasks`, `user_schedules`, `meetings`, `user_settings` |

## Fase 7 — puntos nuevos

- **`resolveBackendUserId`**: id numérico tras sincronizar perfil (agenda ya no depende solo del token).
- **`GET /api/v1/resource-planning`**: capacidad por departamento en Analytics.
- **`GET /api/v1/reports`**: listado en hoja *Export* de Analytics.
- **Bootstrap** al entrar al shell: perfil remoto + agenda.

## Si algo falla

- **401**: vuelve a login (interceptor de sesión).
- **Agenda vacía**: abre Perfil (sync) y pulsa *Sync agenda*; comprueba que el `userId` del perfil sea numérico.
- **Arrays vacíos** en analytics/reports: normal en BD nueva; crea datos con proyectos y check-in.

## Fase 8 — Soporte y APK

1. Home → icono de **alertas** o Perfil → **Abrir centro de soporte**.
2. **New ticket** / **New chat** → POST en Railway.
3. **Refresh lists** y mostrar filas en MySQL (`support_tickets`, `chats`).
4. Generar APK de entrega:
   ```bash
   gradlew.bat :app:assembleRelease
   ```
   Salida: `app/build/outputs/apk/release/app-release.apk` (firmado debug para demo académica).

## Evidencia para entregar

- Capturas: Login, Home con agenda, Analytics con “Synced with server”, Governance heatmap, Perfil settings.
- Captura o export MySQL de 2–3 tablas con timestamps recientes.
