# Architecture Documentation

## Overview

This document describes the architecture of the Client Management API -- a RESTful service built with Quarkus for managing client information. It integrates with the RestCountries external API to fetch demonyms and validate country codes.

> Standalone Mermaid diagram files are available in [`docs/diagrams/`](diagrams/README.md) for reviewers who prefer to render them separately.

## System Context

```mermaid
C4Context
    title System Context Diagram
    
    Person(user, "API Consumer", "Systems or users that consume the Client API")
    
    System(clientApi, "Client Management API", "RESTful service for managing client information")
    
    System_Ext(restCountries, "RestCountries API", "External service for country and demonym data")
    
    SystemDb(database, "Database", "SQL Server (dev/prod), H2 (test)")
    
    Rel(user, clientApi, "Uses", "HTTPS/JSON")
    Rel(clientApi, restCountries, "Validates country code, fetches demonym", "HTTPS")
    Rel(clientApi, database, "Reads/Writes")
```

## Layer Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        direction LR
        RES[ClientResource<br/>JAX-RS Endpoints]
        DTO[DTOs<br/>Request / Response]
        EXC[GlobalExceptionHandler<br/>ValidationExceptionMapper]
    end
    
    subgraph "Business Layer"
        direction LR
        SVC[ClientService<br/>CRUD + Soft Delete]
        VAL[Bean Validation<br/>Constraints]
    end
    
    subgraph "Data Access Layer"
        direction LR
        REP[ClientRepository<br/>Panache]
        ENT[Client Entity<br/>JPA]
    end
    
    subgraph "Integration Layer"
        direction LR
        CS[CountryService]
        RC[RestCountriesClient<br/>MicroProfile REST]
    end
    
    subgraph "Infrastructure"
        direction LR
        DB[(SQL Server / H2)]
        EXT[restcountries.com]
    end
    
    RES --> SVC
    DTO --> RES
    EXC --> RES
    SVC --> REP
    SVC --> CS
    SVC --> VAL
    CS --> RC
    REP --> ENT
    REP --> DB
    RC --> EXT
    
    style RES fill:#4a9eff
    style SVC fill:#4aff9e
    style REP fill:#ff9e4a
    style CS fill:#ff4a9e
```

## Component Details

### Resource Layer

`ClientResource` exposes all REST endpoints under `/api/v1/clients`. Every response is wrapped in `ApiResponse<T>` for a consistent format. Input validation is handled by Bean Validation annotations on the DTO classes.

### Service Layer

`ClientService` contains the business logic:
- Duplicate checks for email and phone (only among active clients)
- Country code validation via `CountryService` before persisting
- Soft delete (sets `active = false` instead of removing the row)

`CountryService` wraps the RestCountries external API to fetch demonyms and validate ISO 3166-1 alpha-2 country codes.

### Repository Layer

`ClientRepository` extends Panache's `PanacheRepository<Client>` and adds custom queries. All retrieval methods filter by `active = true` so soft-deleted clients are excluded.

### Exception Handling

`GlobalExceptionHandler` maps domain exceptions to HTTP status codes:

| Exception | HTTP Status |
|-----------|-------------|
| `ClientNotFoundException` | 404 Not Found |
| `DuplicateEmailException` | 409 Conflict |
| `DuplicatePhoneException` | 409 Conflict |
| `InvalidCountryCodeException` | 400 Bad Request |
| `CountryServiceException` | 503 Service Unavailable |
| `ConstraintViolationException` | 400 Bad Request |
| `PersistenceException` | 409 Conflict |

## Data Flow

### Create Client

```mermaid
flowchart TD
    A[POST /api/v1/clients] --> B{Bean Validation}
    B -->|Invalid| C[400 Bad Request]
    B -->|Valid| D{Email exists<br/>among active?}
    D -->|Yes| E[409 Conflict]
    D -->|No| F{Phone exists<br/>among active?}
    F -->|Yes| G[409 Conflict]
    F -->|No| H{Valid country code?<br/>RestCountries API}
    H -->|Invalid| I[400 Bad Request]
    H -->|Valid| J[Set demonym from API]
    J --> K[Generate UUID]
    K --> L[Persist + Flush]
    L --> M[201 Created + ApiResponse]

    style A fill:#e1f5fe
    style M fill:#c8e6c9
    style C fill:#ffcdd2
    style E fill:#ffcdd2
    style G fill:#ffcdd2
    style I fill:#ffcdd2
```

### Update Client

```mermaid
flowchart TD
    A[PATCH /api/v1/clients/:uuid] --> B{Bean Validation}
    B -->|Invalid| C[400 Bad Request]
    B -->|Valid| D{Active client<br/>with UUID?}
    D -->|No| E[404 Not Found]
    D -->|Yes| F{Email changed?}
    F -->|Yes| G{New email unique<br/>among active?}
    G -->|No| H[409 Conflict]
    G -->|Yes| I{Phone changed?}
    F -->|No| I
    I -->|Yes| J{New phone unique<br/>among active?}
    J -->|No| K[409 Conflict]
    J -->|Yes| L{Country changed?}
    I -->|No| L
    L -->|Yes| M{Valid country code?<br/>RestCountries API}
    M -->|Invalid| N[400 Bad Request]
    M -->|Valid| O[Update demonym]
    O --> P[Update fields + timestamp]
    L -->|No| P
    P --> Q[200 OK + ApiResponse]

    style A fill:#e1f5fe
    style Q fill:#c8e6c9
    style C fill:#ffcdd2
    style E fill:#ffcdd2
    style H fill:#ffcdd2
    style K fill:#ffcdd2
    style N fill:#ffcdd2
```

### Soft Delete

Deleting a client sets `active = false` and updates the timestamp. The row stays in the database but is excluded from all queries. This allows email/phone reuse after deactivation.

## Database Schema

```mermaid
erDiagram
    CLIENTS {
        bigint id PK "Auto-generated (sequence)"
        varchar_36 uuid UK "UUID v4, exposed as public ID"
        varchar_100 first_name "Required"
        varchar_100 second_name "Optional"
        varchar_100 first_surname "Required"
        varchar_100 second_surname "Optional"
        varchar_255 email "Required, unique among active"
        varchar_500 address "Required"
        varchar_20 phone "Required, unique among active"
        varchar_2 country_code "ISO 3166-1 alpha-2"
        varchar_100 demonym "Fetched from RestCountries"
        boolean active "Soft delete flag, default true"
        timestamp created_at "Set on creation"
        timestamp updated_at "Set on every update"
    }
```

## API Endpoints

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| POST | `/api/v1/clients` | Create a new client | 201 |
| GET | `/api/v1/clients` | List all active clients | 200 |
| GET | `/api/v1/clients/{uuid}` | Get client by UUID | 200 |
| GET | `/api/v1/clients/country/{code}` | Filter by country code | 200 |
| PATCH | `/api/v1/clients/{uuid}` | Update client (email, address, phone, country) | 200 |
| DELETE | `/api/v1/clients/{uuid}` | Soft-delete client | 200 |
| GET | `/api/v1/clients/count` | Count active clients | 200 |

All responses follow the `ApiResponse` format:

```json
{
  "success": true,
  "message": "Clients retrieved successfully",
  "data": [ ... ],
  "timestamp": "2026-02-20T22:17:38",
  "errors": null
}
```

## Deployment

### Development / Test

Development connects to SQL Server via Docker. Tests use an H2 in-memory database so no Docker is needed to run `mvn test`.

```
mvn quarkus:dev     # starts on http://localhost:8080
mvn test            # runs 43 unit/integration tests against H2
```


## Design Decisions

1. **UUID as public ID** -- Internal `bigint` IDs stay in the database; clients only see UUIDs. This avoids exposing sequential IDs.
2. **Soft delete** -- Setting `active = false` preserves audit history and allows email/phone reuse for deactivated records.
3. **Flush after persist** -- Calling `flush()` immediately after `persist()` forces constraint violations to surface in the same request instead of silently failing.
4. **Country validation before update** -- When the country code changes, the demonym is fetched first. If the code is invalid, the exception fires before any field is modified.
5. **SQL Server for dev/prod, H2 for tests** -- SQL Server provides a production-grade database. H2 keeps tests fast and independent of external services.

---
