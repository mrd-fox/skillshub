# 🎓 SkillsHub — Backend Platform

![Java](https://img.shields.io/badge/Java-21-007396)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-black)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Overview

**SkillsHub** is an online learning platform where students enroll in courses created and managed by tutors.

This repository contains the **modular monolithic backend**, designed with **strict hexagonal architecture (Ports &
Adapters)**.  
The codebase is structured to ensure **clear separation of concerns**, long-term maintainability, and future
scalability.

---

## Key Functionalities

### Course Management

- Course creation, update, and publication
- Structured content: sections and chapters with ordering
- Access rules enforced at application level (role-based)

### User Management

- Internal user profiles managed by the backend
- Authentication delegated to an external IAM (Keycloak via Gateway)
- Backend remains **IAM-agnostic**

### Video Orchestration

- Video metadata persisted in database
- Upload handled externally (Vimeo)
- Backend controls video lifecycle (`PENDING`, `PROCESSING`, `READY`, `FAILED`)
- Asynchronous processing via message queues

### Asynchronous Messaging

- Event-driven workflows using RabbitMQ
- Delayed and retryable background processing

### Observability

- Metrics exposed via Spring Boot Actuator
- Prometheus scraping
- Grafana dashboards
- Centralized logging with Loki and Promtail

---

## Architecture

The backend strictly follows **Hexagonal Architecture**.

    domain → pure business logic
    application → use cases + ports
    adapter → web, persistence, messaging

### Architectural Principles

- Domain layer is framework-free
- No JPA entities or web DTOs in use cases
- Mapping handled exclusively by adapters
- One use case per module, multiple input ports
- Backend does **not** issue JWT tokens

---

## Technology Stack

| Technology       | Purpose                               |
|------------------|---------------------------------------|
| Java 21          | Core language                         |
| Spring Boot      | Application framework                 |
| Spring Security  | Resource server (JWT validation only) |
| PostgreSQL       | Relational persistence                |
| Liquibase        | Database schema migrations            |
| RabbitMQ         | Asynchronous messaging                |
| Vimeo            | Video hosting                         |
| Prometheus       | Metrics collection                    |
| Grafana          | Monitoring dashboards                 |
| Loki & Promtail  | Log aggregation                       |
| Docker / Compose | Containerized environments            |

---

## Database Strategy

The backend uses **multiple PostgreSQL databases**, separated by functional responsibility:

- Course data
- User profiles
- Media metadata

This separation improves isolation, clarity, and future evolvability.

---

## Environment & Deployment Modes

The backend supports multiple execution modes:

- **Local mode** — application runs locally
- **Hybrid mode** — Gateway runs locally or in Docker
- **Full Docker mode** — all components containerized

Ports, credentials, and infrastructure endpoints are **fully driven by environment variables**.  
No hard-coded configuration is required.

---

## Database Migrations

Database schema evolution is managed with **Liquibase**.

Migrations are executed via the **Makefile**, ensuring:

- Consistent execution
- Reproducible environments
- Clear separation between services

---

## Observability & Monitoring

The application exposes operational data via:

- Spring Boot Actuator endpoints
- Prometheus-compatible metrics
- Preconfigured Grafana dashboards
- Centralized log aggregation (Loki)

This provides full visibility into application health, performance, and background processing.

---

## Security Model

- Authentication is handled externally (Keycloak)
- JWT tokens are **validated**, never issued, by the backend
- Identity and roles are propagated by the Gateway
- Backend remains decoupled from the IAM provider

---

## CI/CD & Development Workflow

### Git Workflow

The project follows a **structured Git flow**:

- `dev` is the **main development branch**
- `main` represents **stable, deployable releases**
- Feature branches are created from `dev`

All integrations into `dev` are performed using **squash merge**, ensuring:

- One commit per feature or ticket
- Clean and readable history
- Easier traceability of changes

---

### Continuous Integration & Code Quality

Continuous Integration is handled via **GitHub Actions**.

On each Pull Request targeting the `dev` branch:

- The project is built
- Automated tests are executed
- **Static code analysis is performed using SonarCloud**

This guarantees that only validated and quality-checked code is merged into the development branch.

---

### Delivery Model

- `dev` is used for ongoing development and integration
- `main` is reserved for versioned, deployable states
- Deployment is explicit and controlled, not automated

---

## License

This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for details.

---

## Author

**Marina Darde**  
Initial development  
GitHub: https://github.com/mrd-fox/skillshub
