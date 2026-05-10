# GeoTracker Analyse

Analysis dashboard for GPS tracking data collected by the GeoTracker app.
Renders aggregate stats, per-session detail with map and synced charts, and
deploys as a WAR to an external Tomcat.

## Features

- **Dashboard** — overall stats, sport-type breakdown, per-user stats,
  monthly trend, records, recent sessions, planned events.
- **Session detail** — Leaflet map of the GPS track + Chart.js charts for
  heart rate, elevation, and speed sharing a numeric distance axis.
- **Synced zoom & pan** — wheel/pinch zoom, drag pan, shift-drag box-zoom;
  all charts stay aligned. "Reset zoom" restores the full range.
- **Hover-to-locate** — hovering any chart drops a marker on the map at the
  matching GPS point.
- **Point details panel** — live side panel beside the map showing every
  available field for the hovered point: time, speed, elevation, HR, slope,
  temperature, humidity, wind (km/h + cardinal), weather (text + WMO code),
  pressure, sea-level pressure, barometric altitude, GPS accuracy,
  satellite count.

## Stack

- Java 26, Spring Boot 4.0.5 (WebMVC, Data JPA, Thymeleaf)
- PostgreSQL (native queries against `gps_tracking_points`,
  `tracking_sessions`, `users`, `lap_times`)
- Bootstrap 5, Leaflet 1.9, Chart.js 4, chartjs-plugin-zoom 2
- Gradle, packaged as `analyse.war` for external Tomcat
  (`bootWar` is disabled)

## Configuration

Database connection is read from environment variables; nothing sensitive
lives in the repo.

| Variable      | Default                                            |
| ------------- | -------------------------------------------------- |
| `DB_URL`      | `jdbc:postgresql://localhost:5432/geotracker`      |
| `DB_USER`     | `geotracker`                                       |
| `DB_PASSWORD` | *(required, no default)*                           |

The app expects a pre-existing schema; `spring.jpa.hibernate.ddl-auto=none`.

## Run locally

```sh
# Windows (PowerShell)
$env:DB_PASSWORD = "..."
./gradlew bootRun

# Linux/macOS
DB_PASSWORD=... ./gradlew bootRun
```

Then open http://localhost:8080/.

## Build the WAR

```sh
./gradlew clean war
# -> build/libs/analyse.war
```

## Deploy to Tomcat

1. Copy `build/libs/analyse.war` into Tomcat's `webapps/` directory.
2. Set `DB_URL`, `DB_USER`, `DB_PASSWORD` in Tomcat's environment
   (`setenv.sh` / `setenv.bat`, or `JAVA_OPTS=-D...`).
3. The app is reachable at `http://<tomcat-host>:<port>/analyse/`.

`ServletInitializer` extends `SpringBootServletInitializer`, so the WAR
boots correctly under an external servlet container.

## Project layout

```
src/main/java/at/co/netconsulting/analyse/
  AnalyseApplication.java      # Spring Boot entry point
  ServletInitializer.java      # WAR bootstrap for Tomcat
  controller/                  # AnalysisController (HTML + /api/session/{id}/track)
  service/AnalysisService.java # aggregation & DTO mapping
  repository/                  # JPA repos with native queries
  entity/                      # JPA entities mapping the GeoTracker schema
  dto/                         # view-model records (TrackPoint, SessionDetail, ...)
src/main/resources/
  templates/                   # Thymeleaf views (dashboard, session-detail)
  application.properties       # env-var-driven datasource config
```

## API

| Path                          | Returns                                |
| ----------------------------- | -------------------------------------- |
| `GET /`                       | Dashboard (HTML)                       |
| `GET /session/{sessionId}`    | Session detail page (HTML)             |
| `GET /api/session/{id}/track` | Track points as JSON `TrackPoint[]`    |
