# Vantage PMO — Android (`MDEPS_APPLICATION`)

Cliente Android (Jetpack Compose) conectado al backend desplegado en **Railway**:

`https://mdeps-back-end-production-1d49.up.railway.app/`

Arquitectura modular por contexto: **iam**, **pm**, **pto**, **gro**, **ass**, **core/network**.

## Requisitos

- Android Studio (AGP reciente del proyecto)
- JDK 17+
- Dispositivo o emulador con red

## Compilar

```bash
./gradlew :app:assembleDebug
# Windows:
gradlew.bat :app:assembleDebug
```

## Script de demo para el profesor (~8 min)

1. **Acceso** — `Sign up` o `Sign in`. Verificar que la sesión guarda token (`DataStore`) y que llamadas llevan `Authorization: Bearer …`.
2. **Perfil** — Tab *Profile*: datos vía `GET/PATCH profiles`, KPIs con `GET stats?userId=`, preferencias con `GET/PUT user-settings` (tema, idioma, notificaciones).
3. **Portafolio** — Tab *Home* / *Projects*: `GET/POST/PUT projects`, pull-to-refresh sincroniza con Railway + Room.
4. **Kanban** — Abrir proyecto → tablero: `GET boards`, `GET/POST/PUT/DELETE tasks`.
5. **Roadmap** — Hitos locales + gobernanza por proyecto.
6. **Analytics** — `GET analytics`, `GET stats` (portafolio), overlay remoto + etiqueta “Synced with server”.
7. **Governance** — `GET compliance-metrics`, `GET risks`, **heatmap** `GET risk-matrix`, pull-to-refresh.
8. **Agenda en Home** — `GET meetings`, `GET user-schedules/user/{userId}`; botones *Sync agenda*, *Save check-in* y *Schedule meeting* (`POST`).
9. **Fase 7** — `GET resource-planning` + `GET reports`; bootstrap de sesión; ver [DEMO.md](DEMO.md).
10. **Fase 8** — Centro de soporte: `GET/POST support-tickets`, `GET/POST chats` (campana Home / Perfil). APK release: `gradlew.bat :app:assembleRelease`.
11. **Fase 9** — `GET/POST chat-messages` en el centro de soporte.
12. **Pruebas** — Guía completa fases 1–9: [PRUEBAS.md](PRUEBAS.md).

### Qué mostrar en MySQL (Railway)

Tablas típicas con filas nuevas tras la demo: `users`, `profiles`, `projects`, `collaboration_tasks`, `risks`, `user_settings`, `user_schedules`, `meetings` (si se crearon eventos).

### Errores de red

- Sin token: algunos endpoints devuelven vacío o 404; la app mantiene datos en **Room/DataStore** y muestra **Snackbar** en Home, Kanban, Analytics, Governance y Perfil.
- `401`: interceptor global cierra sesión y vuelve a login.

## Endpoints integrados (resumen)

| Área | Endpoints |
|------|-----------|
| IAM | sign-in, sign-up |
| PM | profiles, stats, user-settings |
| PTO | projects, boards, tasks, meetings, user-schedules |
| GRO | risks, risk-matrix |
| ASS | analytics, compliance-metrics, resource-planning, stats |
| PTO reports | `GET /reports` (export Analytics) |
| PM support | `support-tickets`, `chats` |

## Estructura de red

- `RetrofitClient` + `NetworkServices` (iam, pm, pto, ass, gro, agenda)
- `AuthInterceptor` / `UnauthorizedInterceptor`
- Repositorios `ApiBacked*` con fallback local (Room)

## Equipo / entrega

Proyecto académico MDEPS — frontend Android + backend compartido (API REST JSON).
