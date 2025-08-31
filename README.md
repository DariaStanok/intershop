# Intershop v4.0 — Store + Payment Service + Authorization Server (Concise)

This is a **reactive educational online store** built as a **multi-module Gradle project**.  
It consists of three modules:  

- **Store** — product catalog, cart, and orders (JSON REST API)  
- **Payment Service** — standalone reactive service with Redis caching and OpenAPI-first workflow  
- **Authorization Server** — OAuth2 provider for buyers and inter-service communication  

All modules are packaged as **executable JARs** with **embedded Netty**.

---

## Modules

### Store
- Reactive stack: **Spring WebFlux + Spring Data R2DBC**  
- Responsibilities: items (catalog, search, sort, paginate), cart (INC/DEC/REMOVE via cartId), orders (create/list/get)  
- Communicates with Payment Service using **generated OpenAPI client**  
- Secured with **Spring Security** (buyer login) and **OAuth2 client credentials flow**  

### Payment Service
- **Reactive REST API** on Spring WebFlux  
- **Redis-backed caching** and idempotency support  
- Contract-first: **OpenAPI spec** defines client and server  

### Authorization Server
- Based on **Spring Authorization Server**  
- Provides OAuth2 flows:  
  - Password login for buyers  
  - Client credentials flow for Store ↔ Payment Service  

---

## Technologies
- **Java 21**  
- **Spring Boot 3.5.3**  
- **Gradle (multi-module)**  
- **Spring WebFlux**  
- **Spring Data R2DBC (PostgreSQL)**  
- **Spring Data Redis**  
- **Spring Authorization Server**  
- **OpenAPI Generator**  
- **ModelMapper**  
- **JUnit 5 + Spring Boot Test + Reactor Test + WebTestClient**

---

## Public API — Store
- `GET /api/items` — query, sort (alphabet|price), paginate  
- `GET /api/items/{id}`  
- `GET /api/cart/items?cartId={id}`  
- `POST /api/cart/items?cartId={id}` — body: `{ itemId, action: INC|DEC|REMOVE }`  
- `POST /api/orders?cartId={id}`  
- `GET /api/orders`  
- `GET /api/orders/{id}`  

## Public API — Payment Service
- `POST /api/payments/intents` — create a payment intent for an order  
- `POST /api/payments/confirm/{intentId}` — confirm a payment intent  

## Public API — Authorization Server
- `POST /oauth2/token` — obtain access token (password / client credentials)  
- `POST /oauth2/introspect` — token introspection  

---

## Configuration
All configuration is centralized in the root **`application.yml`**.  
Sensitive values (database, Redis, OAuth2 credentials) are expected via **environment variables**.  

---

## Development Assumptions
- PostgreSQL and Redis available locally (e.g. via Docker)  
- Environment variables or config files provide credentials  
- Responses are **JSON only** (no server-side templates)  

---

## Quick Start

1. Clone repository:  
   ```bash
   git clone https://github.com/your-org/intershop.git
   cd intershop
   ```

2. Start dependencies (example with Docker):  
   ```bash
   docker run -d --name postgres -e POSTGRES_DB=intershop -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -p 5432:5432 postgres:16
   docker run -d --name redis -p 6379:6379 redis:7
   ```

3. Run modules (each in a separate terminal):  
   ```bash
   ./gradlew :authorization-server:bootRun
   ./gradlew :payment-service:bootRun
   ./gradlew :store:bootRun
   ```

4. Access APIs:  
   - Store → http://localhost:8080/api/items  
   - Payment Service → http://localhost:8081/api/payments/intents  
   - Authorization Server → http://localhost:9000/oauth2/token  
