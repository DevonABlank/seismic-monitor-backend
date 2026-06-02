# Global Seismic Monitor

A full-stack situational awareness dashboard that ingests live earthquake data from the USGS feed, persists it to PostgreSQL, and surfaces it through a paginated, filterable REST API consumed by a React/TypeScript operator dashboard.

Built to demonstrate production-shaped full-stack architecture using Java, Spring Boot, and TypeScript — the core stack required for mission-system development on programs like C2IE.

---

## Architecture

```
USGS Earthquake Feed (GeoJSON)
        │
        │  @Scheduled (every 60s)
        ▼
Spring Boot Backend (Java 17)
  ├── SeismicEventService     — fetches, deduplicates, persists events
  ├── SeismicEventRepository  — Spring Data JPA queries + pagination
  ├── SeismicEventController  — REST endpoints with filtering
  └── Swagger / OpenAPI UI    — auto-generated API documentation
        │
        │  HTTP / REST (JSON)
        ▼
PostgreSQL (Docker)
  └── seismic_events table
        │
        │  HTTP GET /api/events
        ▼
React + TypeScript Frontend
  ├── Live metric cards       — total events, max magnitude, tsunami alerts
  ├── Paginated data table    — sortable, filterable by magnitude
  ├── Magnitude breakdown     — bar chart by severity tier
  ├── Active alerts sidebar   — highlights significant events
  └── System status panel     — API/DB/feed health indicators
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend language | Java 17 |
| Backend framework | Spring Boot 3.5 |
| Data access | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| HTTP client | Spring WebFlux WebClient |
| API documentation | springdoc-openapi / Swagger UI |
| Infrastructure | Docker Compose |
| Frontend framework | React 18 + TypeScript (strict) |
| Frontend build tool | Vite |
| HTTP client (FE) | Axios |
| Data source | USGS Earthquake Feed (free, no auth) |

---

## Features

- **Live data ingestion** — Spring `@Scheduled` job fetches the USGS all-day GeoJSON feed every 60 seconds and persists new events, skipping duplicates via USGS ID check
- **Paginated REST API** — `GET /api/events?page=0&size=20&minMagnitude=3.0` with server-side filtering and descending time sort
- **Auto-generated API docs** — Swagger UI available at `/swagger-ui/index.html`
- **Operator dashboard** — dark, high-contrast UI designed for information density and fast triage, mirroring mission-system UI patterns
- **Live auto-refresh** — frontend polls the API every 60 seconds, displaying last sync time
- **Magnitude filtering** — filter events by M2+, M3+, M4+, M5+ with instant UI feedback
- **Dockerized infrastructure** — single `docker compose up -d` command spins up PostgreSQL

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker Desktop
- Node 18+

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Start the Spring Boot backend

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. On first run, Hibernate auto-creates the `seismic_events` table and the scheduler immediately fetches live data from USGS.

### 3. Verify the API

```
http://localhost:8080/api/events
http://localhost:8080/api/events/count
http://localhost:8080/swagger-ui/index.html
```

### 4. Start the frontend

```bash
cd ../seismic-monitor-frontend
npm install
npm run dev
```

Dashboard available at `http://localhost:5173`

---

## API Reference

### `GET /api/events`

Returns a paginated list of seismic events sorted by event time descending.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | int | 0 | Page number (zero-indexed) |
| `size` | int | 20 | Events per page |
| `minMagnitude` | double | null | Filter by minimum magnitude |

**Example response:**
```json
{
  "content": [
    {
      "id": 1,
      "usgsId": "nc75369806",
      "place": "7 km ESE of Cloverdale, CA",
      "magnitude": 3.69,
      "depth": 5.32,
      "latitude": 38.783,
      "longitude": -122.936,
      "alert": null,
      "tsunami": false,
      "significance": 233,
      "eventTime": "2026-06-02T16:44:19.010Z",
      "fetchedAt": "2026-06-02T18:42:20.781Z"
    }
  ],
  "totalElements": 259,
  "totalPages": 13,
  "number": 0
}
```

### `GET /api/events/count`

Returns total number of events in the database as a plain integer.

---

## Project Structure

```
src/main/java/com/seismic/monitor/
├── MonitorApplication.java        — Spring Boot entry point, enables scheduling
├── model/
│   └── SeismicEvent.java          — JPA entity mapping to seismic_events table
├── repository/
│   └── SeismicEventRepository.java — Spring Data JPA repository with custom queries
├── service/
│   └── SeismicEventService.java   — USGS fetch logic, deduplication, persistence
└── controller/
    └── SeismicEventController.java — REST endpoints, CORS config, pagination
```

---

## Design Decisions

**Why Spring Boot?** Spring Boot is the dominant enterprise Java framework and is explicitly required on the target program. Using Spring Data JPA eliminates boilerplate SQL while demonstrating understanding of ORM patterns, entity mapping, and query derivation.

**Why USGS earthquake data?** The USGS feed is free, requires no authentication, and updates continuously — providing genuine real-time data without API key management. More importantly, the data structure (severity tiers, geographic coordinates, alert levels, tsunami flags) directly mirrors the kinds of multi-domain awareness data surfaced in C2 mission systems.

**Why this UI aesthetic?** The dashboard is intentionally designed as a dense operator interface rather than a consumer application — high information density, dark theme, severity-coded indicators, and triage-oriented layout. This mirrors the UI patterns used in command and control, SOC, and situational awareness systems.

**Why Docker for PostgreSQL?** Running infrastructure in containers is standard practice in DevSecOps-oriented programs. Using Docker Compose demonstrates familiarity with containerized development environments without overcomplicating the local setup.

---

## Frontend Repository

[seismic-monitor-frontend](https://github.com/DevonABlank/seismic-monitor-frontend)

---

## Author

Devon Blank — [LinkedIn](https://linkedin.com/in/devonblank) · [GitHub](https://github.com/DevonABlank)
