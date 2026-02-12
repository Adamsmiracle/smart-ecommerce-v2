# Smart E-Commerce API (smart-ecommerce-api-v1)

A Spring Boot based backend for a sample e-commerce application. This repository provides both REST and GraphQL endpoints for products, users, carts, orders, reviews, wishlist, and addresses.

## Quick start (development)

Prerequisites:
- Java 21
- Maven 3.8+
- PostgreSQL (matching DB config in `src/main/resources/application-dev.yaml`)

1. Build the project

```powershell
cd C:\Users\MiracleAdams\projects\smart-ecommerce-api-v1
mvn -DskipTests clean package
```

2. Configure the database
- Update `src/main/resources/application-dev.yaml` with your DB connection (URL, username, password).
- Ensure the database schema is created (project contains `schema.sql` under resources used at startup for dev profile).

3. Run the application

```powershell
mvn -Dspring.profiles.active=dev spring-boot:run
```

4. Useful endpoints
- REST: http://localhost:8080/api/
- GraphQL: http://localhost:8080/graphql (POST)
- OpenAPI/Swagger UI: http://localhost:8080/swagger-ui/index.html

## Project layout
- `src/main/java/com/miracle/smart_ecommerce_api_v1/` — application code
  - `config/` — Spring and GraphQL configuration
  - `controller/` — REST controllers
  - `domain/` — domain areas (user, product, order, review, etc.)
  - `security/` — JWT provider and authentication pieces
  - `exception/` — centralized `GlobalExceptionHandler` and `ApiError`/error codes
  - `common/response` — `ApiResponse` wrapper used across endpoints
- `src/main/resources/graphql/schema.graphqls` — GraphQL schema

## Centralized error handling
All exceptions are handled by `GlobalExceptionHandler` (a `@ControllerAdvice`) and returned with a consistent JSON structure using `ApiResponse<T>` and `ApiError`. Each response includes a correlationId header (`X-Correlation-Id`) provided by `CorrelationIdFilter` so you can trace a request across logs.

See `PERFORMANCE_REPORT.md` for performance guidance comparing REST and GraphQL.



