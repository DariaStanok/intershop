## Intershop v3.0 — Store + Payment Service (Concise)

This is a reactive educational online store (Store) plus a standalone Payment Service, built with Java 21, Spring Boot 3.5.3, WebFlux and R2DBC.
The Store exposes JSON REST endpoints for a non-blocking product catalog, cart, and order management.
The Payment Service is a RESTful component with Redis caching and an OpenAPI-first workflow. Both modules are packaged as executable JARs (Netty).

## Modules
- Store
  - Reactive stack: Spring WebFlux + Spring Data R2DBC
  - Responsibilities: items (catalog, search, sort, paginate), cart (INC/DEC/REMOVE via cartId), orders (create/list/get)
  - Communication: uses generated OpenAPI client to call Payment Service
- Payment Service
  - Reactive REST API on Spring WebFlux
  - Redis-backed caching and idempotency support where applicable
  - OpenAPI spec is the contract for client generation

## Technologies
- Java 21
- Spring Boot 3.5.3
  - Store: Spring WebFlux, Spring Data R2DBC (JSON REST)
  - Payment Service: Spring WebFlux, Spring Data Redis, OpenAPI
- Gradle (multi-module)
- PostgreSQL (R2DBC driver)
- Redis
- Embedded Netty
- ModelMapper
- JUnit 5 + Spring Boot Test + Reactor Test + WebTestClient

## Public API — Store (JSON)
- GET /api/items — query, sort (alphabet|price), paginate
- GET /api/items/{id}
- GET /api/cart/items?cartId={id}
- POST /api/cart/items?cartId={id} — body: { itemId, action: INC|DEC|REMOVE }
- POST /api/orders?cartId={id}
- GET /api/orders
- GET /api/orders/{id}

## Public API — Payment Service
- POST /api/payments/intents — create a payment intent for an order
- POST /api/payments/confirm/{intentId} — confirm a payment intent

## Configuration (env / application.yml placeholders)
- Store
  - spring.r2dbc.url
  - spring.r2dbc.username
  - spring.r2dbc.password
  - server.port (default 8080)
- Payment Service
  - spring.data.redis.host
  - spring.data.redis.port
  - spring.data.redis.password
  - server.port (default 8081)

## Development Assumptions
- PostgreSQL and Redis are available locally for development (e.g., via Docker).
- Environment variables or config files provide credentials and connection details.
- No server-side templates; responses are JSON only.