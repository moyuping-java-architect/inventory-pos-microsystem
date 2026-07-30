## PSI Platform — Common Modules

A microservices platform for retail management (Purchase, Sale, Inventory) built for African markets. This repository contains the **shared foundation modules** used by all 12+ microservices.

### What This Project Does

PSI is a full retail management system used in Zambia. It handles purchasing from suppliers, selling to customers, inventory tracking, POS (point of sale), and financial reporting. The system supports multiple tenants (different shops/companies) running on the same platform.

### Tech Stack

| Technology | Version |
|---|---|
| Java | 21 (with Virtual Threads) |
| Spring Boot | 3.2 |
| Spring Cloud Alibaba | 2023.0.1 |
| MyBatis-Plus | 3.5.6 |
| Redis | Lua scripts for distributed locking |
| RabbitMQ | 30+ consumers, 10 dead-letter queues |
| Nacos | Service discovery & config |
| MySQL / SQLite | POS offline sync |
| OpenAPI / Swagger | Auto-generated API docs |

### Module Overview

**psi-common-core** — The base library. Contains shared utilities, Redis distributed lock (using Lua scripts), message idempotency, JWT auth, unified API response wrapper, and JDK 21 virtual thread executor with context propagation.

**psi-common-starter-tenant** — Multi-tenant isolation across 5 layers: HTTP requests, Feign calls, MQ producers, MQ consumers, and async/scheduled threads. Uses Alibaba TTL to pass tenant context everywhere automatically.

**psi-common-starter-trace** — Distributed tracing using AspectJ Load-Time Weaving. Tracks every request across all services with traceId/spanId. Only logs when something is slow or fails — stays quiet during normal operation.

**psi-common-starter-mq** — RabbitMQ setup for the whole system. Defines all exchanges, queues, and dead-letter queues. Includes a TTL-aware RabbitTemplate that carries tenant context in every message.

**psi-common-starter-mybatis** — MyBatis-Plus configuration with automatic tenant SQL injection (every query gets `AND tenant_id = ?` automatically), slow SQL detection, entity auto-fill, and batch utilities.

**psi-common-starter-async** — Thread pool configuration with tenant context propagation. Includes a unified MQ message facade that auto-fills messageId, tenantId, and operatorId from the current user context.

**psi-common-starter-order-rule** — Universal document/order management. Handles the full lifecycle: draft → submit → approve → execute → complete. Supports 12 document types (purchase orders, sales, stock transfers, etc.) with strategy pattern for post-completion notifications.

**psi-common-starter-nacos** — Nacos service discovery and centralized configuration bootstrap.

**psi-common-starter-log** — Centralized log configuration. Each service gets its own log directory with configurable retention, file size limits, and optional async logging.

**psi-common-starter-doc** — OpenAPI/Swagger auto-configuration. Add this starter and every service gets a consistent API documentation page at `/swagger-ui.html`.

### Architecture Highlights

**Five-Layer Tenant Isolation** — Tenant context is extracted from HTTP headers at the gateway and automatically propagated through Feign calls, MQ messages, async threads, and scheduled tasks. Every database query is automatically filtered by tenant_id at the SQL level. No tenant data leakage possible.

**Redis Lua Distributed Lock** — Custom distributed lock implementation using Redis Lua scripts for atomic operations. No external dependency like Redisson needed. Supports reentrant locking and automatic expiration.

**AspectJ LTW Tracing** — Unlike Spring AOP (which only intercepts public methods), AspectJ Load-Time Weaving intercepts all methods including private ones. High-frequency methods (>100 calls) are auto-detected and skipped to reduce overhead.

**JDK 21 Virtual Threads** — Uses Java 21's virtual threads for high-concurrency async operations, integrated with Alibaba TTL for seamless tenant context propagation.

**POS Offline Sync** — Bidirectional sync protocol between MySQL (server) and SQLite (POS devices). Supports offline operation with automatic conflict resolution when reconnected.

### Project Structure

```
psi-parent/
├── psi-common-core/              # Base utilities, Redis lock, JWT, virtual threads
├── psi-common-starter-tenant/    # Multi-tenant isolation (5 layers)
├── psi-common-starter-trace/     # Distributed tracing (AspectJ LTW)
├── psi-common-starter-mq/        # RabbitMQ topology & TTL template
├── psi-common-starter-mybatis/   # MyBatis-Plus with tenant SQL injection
├── psi-common-starter-async/     # Async thread pools with context propagation
── psi-common-starter-order-rule/ # Document lifecycle & strategy pattern
├── psi-common-starter-nacos/     # Nacos bootstrap
├── psi-common-starter-log/       # Centralized logging
── psi-common-starter-doc/       # OpenAPI auto-config
└── (14 business microservices)   # Not included in this repo
```

### Author

**Yuping Mo** — Senior Architect
- Location: Lusaka, Zambia (Open to Remote)
- Email: yuping.mo@outlook.com
- GitHub: [moyuping-java-architect](https://github.com/moyuping-java-architect)

### License

Private — All rights reserved.
