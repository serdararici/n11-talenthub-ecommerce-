# N11 TalentHub E-Commerce Project

## Tech Stack
- Java 21 + Spring Boot 3.x
- Maven multi-module project
- PostgreSQL (per service)
- JWT authentication (jjwt)
- Redis (cart service)
- Docker Compose
- React 18 + Vite + Tailwind CSS

## Project Structure
n11-talenthub-ecommerce/
├── CLAUDE.md
├── docker-compose.yml
├── frontend/
└── backend/
    ├── pom.xml (parent)
    ├── auth-service/     (port 8081)
    ├── product-service/  (port 8082)
    ├── cart-service/     (port 8083)
    └── order-service/    (port 8084)

## Microservices

### auth-service (port 8081)
- POST /api/auth/register
- POST /api/auth/login → returns JWT
- GET /api/auth/validate
- POST /api/auth/refresh

### product-service (port 8082)
- GET /api/products?page=0&size=10&category=&search=
- GET /api/products/{id}
- POST /api/products (ADMIN)
- PUT /api/products/{id} (ADMIN)
- DELETE /api/products/{id} (ADMIN)
- GET /api/products/categories

### cart-service (port 8083)
- GET /api/cart
- POST /api/cart/items
- PUT /api/cart/items/{productId}
- DELETE /api/cart/items/{productId}
- DELETE /api/cart

### order-service (port 8084)
- POST /api/orders
- GET /api/orders
- GET /api/orders/{id}
- PUT /api/orders/{id}/status (ADMIN)
- POST /api/orders/{id}/payment (Iyzico)

## Coding Standards
- Always write complete files, never partial snippets
- Use DTOs for all request/response, never expose entities
- Standard API response format:
  { "success": true, "message": "", "data": {}, "timestamp": "" }
- Use @Slf4j for logging
- Use @Valid and Bean Validation for inputs
- Global exception handler with @ControllerAdvice
- Use application.yml not application.properties
- Never hardcode secrets, always use environment variables
- Use Lombok to reduce boilerplate

## Security
- JWT in Authorization header as Bearer token
- Each service validates JWT independently with shared secret
- CORS allow localhost:5173
- BCrypt for password hashing
- Roles: ROLE_USER, ROLE_ADMIN

## Database
- Each service has its own PostgreSQL database
- ddl-auto=update for development
- HikariCP connection pool

## Priority Order
1. backend/pom.xml (parent POM)
2. auth-service (complete)
3. product-service (complete)
4. docker-compose.yml
5. frontend (React)
6. cart-service
7. order-service
8. Iyzico integration
9. Swagger docs
10. Unit tests
11. GitHub Actions CI/CD
12. AWS deployment

## What to Avoid
- No Spring Cloud Gateway or Eureka
- No Kubernetes
- No hardcoded secrets
- No partial code snippets