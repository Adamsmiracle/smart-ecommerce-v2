# Performance Report — REST vs GraphQL

Date: 2026-02-10

## Executive summary

This report compares REST and GraphQL performance for the Smart E-Commerce API. Key trade-offs:

- REST is typically lighter per-request and easier to cache (HTTP-level caching). It's a good fit for simple, cacheable resources (product metadata, categories).
- GraphQL reduces client round-trips by allowing a single query to fetch nested data; this often reduces end-to-end latency for composite views but increases per-request server work and the risk of N+1 database queries without batching (DataLoader).

Use GraphQL when client-side flexibility and fewer round-trips matter; ensure DataLoader, query complexity limits and caching are in place. Use REST for highly cacheable or high-throughput simple endpoints.

---

## Goals and acceptance criteria

- Measure latency (p50/p95/p99), throughput (req/s), error rate, DB queries per request, and payload sizes.
- Representative success criteria examples (adjust to your targets):
  - Reads: p95 < 350ms at 50 concurrent users, error rate < 1%.
  - Writes: average createOrder < 700ms at normal load.
  - DB queries per composite UI request ≤ 5 after batching.

---

## Test methodology

Environment:
- Use a dedicated test/staging environment with realistic dataset and similar hardware as production.
- Use the same DB seed for REST and GraphQL runs.
- Disable caches only if you want to test uncached performance; otherwise test both cached and uncached scenarios.

Tools:
- k6 for load testing (recommended): https://k6.io
- PostgreSQL monitoring (pg_stat_statements) or SQL logging for query counts
- JVM metrics (Micrometer, /actuator/metrics), and a profiler/APM for tracing

Test plan:
1. Warm-up for 2 minutes to reach steady state.
2. Run each scenario (REST and GraphQL) 3 times; collect median results.
3. Scenarios: Smoke, Load (target concurrency), Stress (ramp to saturation).

Metrics to capture:
- Client: p50/p95/p99 latency, mean, max, throughput (req/s), error rate, response size
- Server: CPU %, heap, GC activity, thread pool usage, http.server.requests (Micrometer)
- DB: queries/sec, slow queries, queries per request (pg_stat_statements)
- Logs: correlation ids (X-Correlation-Id) to trace requests

---

## Representative scenarios (map to your app)

### Scenario A — Product list + details + reviews (composite read)
- REST: GET `/api/products?page=0&size=10` + per-product GET details and GET reviews for N products → multiple round-trips
- GraphQL: single query fetching products and nested reviews in one request

### Scenario B — Cart view
- REST: GET `/api/cart/user/{userId}` + per-item product detail calls
- GraphQL: single `cart(userId)` query returning nested product info

### Scenario C — Create order (write heavy)
- REST: POST `/api/orders` with full order payload
- GraphQL: mutation `createOrder(input:{...})`

### Scenario D — Mixed workload
- 80% reads (Scenario A), 20% writes (Scenario C)

---

## k6 test scripts (examples)

Save these scripts and run with `k6 run`. Adjust `vus` and `duration` per environment.

How to run:

```powershell
k6 run k6/graphql-test.js
k6 run k6/rest-test.js
```
---

## How to measure DB queries per request

- Enable `pg_stat_statements` in Postgres and query `pg_stat_statements` before/after test windows.
- Alternatively enable a short-lived SQL log during tests or use an APM/tracing tool to capture DB call counts per correlation id.
- Focus on N+1 detection: GraphQL often causes many small queries unless `DataLoader` batching is used.

---

## Interpretation guide

- If GraphQL shows lower end-to-end latency: success — fewer round-trips overcame the heavier per-request cost.
- If GraphQL has more DB queries or much higher CPU: inspect resolvers for N+1; implement DataLoader batching.
- If REST outperforms GraphQL: consider hybrid approach — keep REST for highly cacheable endpoints and GraphQL for composite views.

---

## Concrete optimizations

For GraphQL:
- Implement `DataLoader` batching and caching for entity lookups (products, users, categories).
- Add query complexity and depth limits (use `graphql-java` instrumentation) to block expensive queries.
- Use persisted queries when possible to reduce parsing/validation overhead.
- Cache frequently-read fields (e.g., product metadata) with Caffeine or Redis and avoid caching sensitive fields (passwords).

For REST:
- Use HTTP caching headers (Cache-Control, ETag) and CDN for static/immutable resources.
- Optimize SQL queries and indexes.

DB and infra:
- Add indexes on frequently filtered columns (product category, user_id, etc.).
- Monitor HikariCP pool usage and tune `maximumPoolSize`.

---

## Next steps & deliverables I can provide

- Tailored `k6` scripts covering your exact GraphQL queries and REST flows (I can generate these automatically from `schema.graphqls` and `API_ENDPOINTS_FULL.md`).
- Implement `DataLoader` wiring in GraphQL resolvers to remove N+1 problems.
- Add GraphQL query complexity/depth limiting to `GraphQLConfig`.
- Generate a one-click harness (PowerShell) that runs REST and GraphQL tests and produces a CSV summary.

Tell me which of the next steps you want and I will implement them.

