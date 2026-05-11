# 🛒 N11 TalentHub Bootcamp — Microservices E-Commerce Application

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=springboot)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?style=for-the-badge&logo=githubactions)

**A production-ready fullstack e-commerce platform built with microservices architecture**

*N11 TalentHub Java & React Bootcamp — Final Project*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Microservices](#-microservices)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Security](#-security)
- [Frontend](#-frontend)
- [Docker & Infrastructure](#-docker--infrastructure)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Payment Integration](#-payment-integration)
- [Testing](#-testing)
- [Environment Variables](#-environment-variables)

---

## 🎯 Overview

This project is a fully functional e-commerce application developed as the final project for the **N11 TalentHub Java & React Bootcamp**. It demonstrates enterprise-level software development practices including:

- **Microservices Architecture** — 4 independent Spring Boot services
- **JWT-based Security** — Stateless authentication across all services
- **Containerization** — Docker & Docker Compose for local and production environments
- **CI/CD Pipeline** — Automated build, test, and deployment via GitHub Actions
- **Payment Integration** — Iyzico payment gateway (sandbox)
- **API Documentation** — Swagger/OpenAPI 3 for all services
- **Clean Code & SOLID** — DTOs, global exception handling, layered architecture

### Features

- 🔐 User registration & login with JWT authentication
- 🛍️ Product listing with pagination, search & category filtering
- 🛒 Shopping cart management (Redis-backed)
- 📦 Order creation and lifecycle management
- 💳 Payment processing with Iyzico
- 👤 Role-based access control (USER / ADMIN)
- 📄 Swagger UI for all microservices
- 🐳 One-command startup with Docker Compose

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      REACT FRONTEND                          │
│              (Vite + React 18 + Tailwind CSS)                │
│                     localhost:5173                           │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST  (JWT Bearer Token)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    NGINX (Port 80)                           │
│               Reverse Proxy / API Gateway                    │
└────┬──────────────┬──────────────┬──────────────┬───────────┘
     │              │              │              │
┌────▼───┐   ┌──────▼─────┐ ┌─────▼──────┐ ┌────▼───────┐
│  Auth  │   │  Product   │ │    Cart    │ │   Order    │
│Service │   │  Service   │ │  Service   │ │  Service   │
│ :8081  │   │   :8082    │ │   :8083    │ │   :8084    │
└────┬───┘   └──────┬─────┘ └─────┬──────┘ └────┬───────┘
     │              │              │              │
     └──────────────▼──────────────┴──────────────┘
                    │
         ┌──────────┴───────────┐
         │                      │
    ┌────▼─────┐          ┌─────▼────┐
    │PostgreSQL│          │  Redis   │
    │  :5432   │          │  :6379   │
    │(per-svc  │          │  (cart)  │
    │  schema) │          └──────────┘
    └──────────┘
```

Each microservice:
- Owns its own **PostgreSQL schema/database**
- Validates **JWT tokens independently** (shared secret via env var)
- Exposes its own **Swagger UI**
- Runs in its own **Docker container**

---

## 🧰 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Primary language |
| Spring Boot | 3.x | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | ORM / database access |
| jjwt | 0.12.x | JWT token creation & validation |
| PostgreSQL | 16 | Relational database |
| Redis | 7 | Cart service cache |
| Lombok | latest | Boilerplate reduction |
| springdoc-openapi | 2.x | Swagger/OpenAPI 3 documentation |
| Maven | 3.9 | Build & dependency management |
| Jib | 3.x | Containerization without Dockerfile |
| iyzipay-java | latest | Iyzico payment SDK |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 18 | UI framework |
| Vite | 5.x | Build tool & dev server |
| React Router | v6 | Client-side routing |
| Axios | latest | HTTP client |
| Tailwind CSS | 3.x | Utility-first CSS framework |
| Context API | - | Global state (Auth, Cart) |

### Infrastructure & DevOps
| Technology | Purpose |
|---|---|
| Docker | Containerization |
| Docker Compose | Local multi-service orchestration |
| GitHub Actions | CI/CD pipeline |
| NGINX | Reverse proxy / static file serving |
| GitHub Container Registry | Docker image hosting |

---

## 🔧 Microservices

### 1. 🔑 Auth Service — Port 8081

Handles user registration, login, and JWT token lifecycle.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user |
| `POST` | `/api/auth/login` | Public | Login, returns JWT |
| `GET` | `/api/auth/validate` | Internal | Validate JWT token |
| `POST` | `/api/auth/refresh` | Authenticated | Refresh access token |

**Request — Register:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePass123",
  "role": "ROLE_USER"
}
```

**Response — Login:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

> Access token TTL: **15 minutes** | Refresh token TTL: **7 days**

---

### 2. 📦 Product Service — Port 8082

Manages product catalog with full CRUD, pagination, search, and category filtering.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/products` | Public | List products (paginated) |
| `GET` | `/api/products/{id}` | Public | Get product by ID |
| `POST` | `/api/products` | ADMIN | Create product |
| `PUT` | `/api/products/{id}` | ADMIN | Update product |
| `DELETE` | `/api/products/{id}` | ADMIN | Delete product |
| `GET` | `/api/products/categories` | Public | List all categories |

**Query Parameters:**
```
GET /api/products?page=0&size=10&category=electronics&search=laptop
```

**Product Entity:**
```json
{
  "id": 1,
  "name": "Laptop Pro 15",
  "description": "High performance laptop",
  "price": 25999.99,
  "stock": 50,
  "imageUrl": "https://...",
  "category": "Electronics",
  "createdAt": "2024-01-01T00:00:00"
}
```

---

### 3. 🛒 Cart Service — Port 8083

Manages shopping carts, backed by **Redis** for high-speed read/write.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/cart` | Authenticated | Get current user's cart |
| `POST` | `/api/cart/items` | Authenticated | Add item to cart |
| `PUT` | `/api/cart/items/{productId}` | Authenticated | Update item quantity |
| `DELETE` | `/api/cart/items/{productId}` | Authenticated | Remove item from cart |
| `DELETE` | `/api/cart` | Authenticated | Clear entire cart |

User identity is extracted from the **JWT token** — no userId parameter needed.

---

### 4. 🧾 Order Service — Port 8084

Manages order lifecycle from creation to delivery, including Iyzico payment.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/orders` | Authenticated | Create order from cart |
| `GET` | `/api/orders` | Authenticated | Get user's orders |
| `GET` | `/api/orders/{id}` | Authenticated | Order detail |
| `PUT` | `/api/orders/{id}/status` | ADMIN | Update order status |
| `POST` | `/api/orders/{id}/payment` | Authenticated | Initiate Iyzico payment |
| `POST` | `/api/orders/payment/callback` | Public | Iyzico webhook callback |

**Order Status Flow:**
```
PENDING → PAID → SHIPPED → DELIVERED
                         ↘ CANCELLED
```

---

### Standard API Response Format

All services return a consistent response envelope:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-01T12:00:00"
}
```

Error responses:
```json
{
  "success": false,
  "message": "Product not found with id: 99",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

---

## 📁 Project Structure

```
n11-talenthub-ecommerce/
├── 📄 CLAUDE.md                    # AI assistant context file
├── 📄 docker-compose.yml           # Full stack local setup
├── 📄 .github/
│   └── workflows/
│       ├── ci.yml                  # Build & test on every push
│       └── cd.yml                  # Deploy on merge to main
│
├── 🌐 frontend/
│   ├── public/
│   ├── src/
│   │   ├── api/                    # Axios instances per service
│   │   │   ├── authApi.js
│   │   │   ├── productApi.js
│   │   │   ├── cartApi.js
│   │   │   └── orderApi.js
│   │   ├── components/             # Reusable UI components
│   │   │   ├── Navbar.jsx
│   │   │   ├── ProductCard.jsx
│   │   │   ├── Pagination.jsx
│   │   │   ├── CartItem.jsx
│   │   │   └── ProtectedRoute.jsx
│   │   ├── pages/                  # Route-level pages
│   │   │   ├── LoginPage.jsx
│   │   │   ├── RegisterPage.jsx
│   │   │   ├── HomePage.jsx
│   │   │   ├── ProductsPage.jsx
│   │   │   ├── ProductDetailPage.jsx
│   │   │   ├── CartPage.jsx
│   │   │   ├── CheckoutPage.jsx
│   │   │   ├── OrdersPage.jsx
│   │   │   └── admin/
│   │   │       └── AdminProductsPage.jsx
│   │   ├── context/
│   │   │   ├── AuthContext.jsx     # JWT storage & user state
│   │   │   └── CartContext.jsx     # Cart state management
│   │   ├── hooks/                  # Custom React hooks
│   │   └── utils/                  # Helpers & constants
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.js
│
└── 🔧 backend/
    ├── pom.xml                     # Parent POM (multi-module)
    │
    ├── auth-service/
    │   ├── src/main/java/com/n11/auth/
    │   │   ├── controller/         # AuthController
    │   │   ├── service/            # AuthService, JwtService
    │   │   ├── repository/         # UserRepository
    │   │   ├── entity/             # User, RefreshToken
    │   │   ├── dto/                # Request & Response DTOs
    │   │   ├── security/           # SecurityConfig, JwtFilter
    │   │   └── exception/          # GlobalExceptionHandler
    │   ├── src/main/resources/
    │   │   └── application.yml
    │   └── pom.xml
    │
    ├── product-service/
    │   ├── src/main/java/com/n11/product/
    │   │   ├── controller/         # ProductController
    │   │   ├── service/            # ProductService
    │   │   ├── repository/         # ProductRepository
    │   │   ├── entity/             # Product
    │   │   ├── dto/
    │   │   ├── security/           # JWT validation filter
    │   │   └── exception/
    │   ├── src/main/resources/
    │   │   ├── application.yml
    │   │   └── data.sql            # Sample product data
    │   └── pom.xml
    │
    ├── cart-service/
    │   ├── src/main/java/com/n11/cart/
    │   │   ├── controller/         # CartController
    │   │   ├── service/            # CartService
    │   │   ├── repository/         # CartRepository (Redis)
    │   │   ├── entity/             # Cart, CartItem
    │   │   ├── dto/
    │   │   └── security/
    │   └── pom.xml
    │
    └── order-service/
        ├── src/main/java/com/n11/order/
        │   ├── controller/         # OrderController
        │   ├── service/            # OrderService, IyzicoPaymentService
        │   ├── repository/         # OrderRepository
        │   ├── entity/             # Order, OrderItem
        │   ├── dto/
        │   ├── security/
        │   └── exception/
        └── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (v24+)
- [Docker Compose](https://docs.docker.com/compose/) (v2+)
- Java 21 (for local development without Docker)
- Node.js 20+ (for frontend local development)

### Quick Start (Docker — Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/n11-talenthub-ecommerce.git
cd n11-talenthub-ecommerce

# 2. Create environment file
cp .env.example .env
# Edit .env with your values (see Environment Variables section)

# 3. Start everything
docker-compose up -d

# 4. Wait ~30 seconds for services to initialize, then check health
docker-compose ps
```

Once running, access the application at:

| Service | URL |
|---|---|
| 🌐 Frontend | http://localhost |
| 🔑 Auth Service | http://localhost:8081 |
| 📦 Product Service | http://localhost:8082 |
| 🛒 Cart Service | http://localhost:8083 |
| 🧾 Order Service | http://localhost:8084 |

### Local Development (Without Docker)

**Backend — each service:**
```bash
cd backend/auth-service
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
# Runs at http://localhost:5173
```

### Useful Commands

```bash
# View logs for a specific service
docker-compose logs -f auth-service

# Restart a single service
docker-compose restart product-service

# Stop everything
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v

# Rebuild images
docker-compose build --no-cache
```

---

## 📚 API Documentation

Every microservice exposes **Swagger UI** at its `/swagger-ui.html` endpoint:

| Service | Swagger URL |
|---|---|
| Auth Service | http://localhost:8081/swagger-ui.html |
| Product Service | http://localhost:8082/swagger-ui.html |
| Cart Service | http://localhost:8083/swagger-ui.html |
| Order Service | http://localhost:8084/swagger-ui.html |

### Testing with Swagger

1. Open Auth Service Swagger → `/api/auth/login` → Execute → copy the `accessToken`
2. Click **Authorize** (🔒 button) → paste `Bearer <your-token>`
3. All secured endpoints are now accessible

---

## 🔐 Security

### JWT Authentication Flow

```
Client                    Service
  │                          │
  ├──POST /auth/login────────▶│
  │◀──── accessToken ─────────┤
  │                          │
  ├──GET /products ──────────▶│
  │  Authorization:           │ Validates JWT with
  │  Bearer <token>           │ shared secret (no DB call)
  │◀──── 200 OK + data ───────┤
```

### Security Features

- **Stateless JWT** — No session storage, each request is self-contained
- **BCrypt Password Hashing** — Industry-standard password encryption
- **Role-based Authorization** — `ROLE_USER` and `ROLE_ADMIN`
- **Independent JWT Validation** — Each service validates tokens without calling Auth Service
- **Shared Secret** — JWT secret distributed via environment variable
- **CORS Configuration** — Only allows requests from the React frontend origin
- **No Hardcoded Secrets** — All sensitive values in environment variables

### Token Details

```yaml
JWT:
  access-token-expiration: 15 minutes
  refresh-token-expiration: 7 days
  algorithm: HS512
  header: Authorization: Bearer <token>
```

---

## 🌐 Frontend

### Pages & Routes

| Route | Page | Access |
|---|---|---|
| `/login` | Login | Public |
| `/register` | Register | Public |
| `/` | Home (featured products) | Public |
| `/products` | Product listing + search + filter | Public |
| `/products/:id` | Product detail + Add to cart | Public |
| `/cart` | Cart management | Authenticated |
| `/checkout` | Order & payment (Iyzico) | Authenticated |
| `/orders` | Order history | Authenticated |
| `/admin/products` | Admin product management | ADMIN only |

### State Management

- **AuthContext** — Stores JWT token, user info, login/logout actions
- **CartContext** — Cart item count, sync with Cart Service

### API Layer

Each service has a dedicated Axios instance with JWT interceptor:

```javascript
// src/api/productApi.js
const productApi = axios.create({
  baseURL: import.meta.env.VITE_PRODUCT_SERVICE_URL
});

// Automatically attach JWT to every request
productApi.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

---

## 🐳 Docker & Infrastructure

### Docker Compose Services

```yaml
services:
  postgres:    # PostgreSQL 16 — shared for all services (separate schemas)
  redis:       # Redis 7 — cart service cache
  auth-service:    # Port 8081
  product-service: # Port 8082
  cart-service:    # Port 8083
  order-service:   # Port 8084
  frontend:    # React app served by NGINX — Port 80
```

### Service Dependencies

```
frontend ──depends_on──▶ (all backend services)
order-service ─────────▶ postgres, redis
cart-service ──────────▶ postgres, redis
product-service ───────▶ postgres
auth-service ──────────▶ postgres
```

### Multi-stage Dockerfile (Backend)

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run (slim JRE image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend NGINX Config

The frontend container uses NGINX to:
1. Serve React static files
2. Proxy `/api/auth/**` → auth-service:8081
3. Proxy `/api/products/**` → product-service:8082
4. Proxy `/api/cart/**` → cart-service:8083
5. Proxy `/api/orders/**` → order-service:8084

---

## ⚙️ CI/CD Pipeline

### GitHub Actions Workflows

#### CI — `ci.yml` (runs on every push & pull request)

```
Push / PR
    │
    ├── Build & Test Backend
    │     ├── Java 21 setup
    │     ├── Maven build (all modules)
    │     └── Run unit tests
    │
    ├── Build Frontend
    │     ├── Node.js 20 setup
    │     ├── npm install
    │     └── npm run build
    │
    └── ✅ CI Passed
```

#### CD — `cd.yml` (runs on merge to `main`)

```
Merge to main
    │
    ├── CI must pass first
    │
    ├── Build Docker Images
    │     ├── auth-service → ghcr.io/user/n11-auth:latest
    │     ├── product-service → ghcr.io/user/n11-product:latest
    │     ├── cart-service → ghcr.io/user/n11-cart:latest
    │     ├── order-service → ghcr.io/user/n11-order:latest
    │     └── frontend → ghcr.io/user/n11-frontend:latest
    │
    ├── Push to GitHub Container Registry (GHCR)
    │
    └── ✅ Images ready for deployment
```

### vs Jenkins

| Feature | GitHub Actions | Jenkins |
|---|---|---|
| Setup | Zero — built into GitHub | Requires dedicated server |
| Configuration | YAML in repo | Groovy DSL / GUI |
| Integration | Native GitHub events | Webhooks needed |
| Runners | Free GitHub-hosted | Self-hosted required |
| Secret management | GitHub Secrets | Jenkins Credentials |

GitHub Actions was chosen for its **simplicity, zero infrastructure overhead**, and tight integration with the source repository.

---

## 💳 Payment Integration

Iyzico payment gateway is integrated in the Order Service using the official **iyzipay-java SDK**.

### Payment Flow

```
User clicks "Pay"
    │
    ├─▶ POST /api/orders/{id}/payment
    │       └── OrderService calls Iyzico Sandbox API
    │
    ├─▶ Iyzico returns payment form URL
    │
    ├─▶ User completes payment on Iyzico page
    │
    └─▶ POST /api/orders/payment/callback (webhook)
            └── Order status updated: PENDING → PAID
```

### Configuration

```yaml
# application.yml
iyzico:
  api-key: ${IYZICO_API_KEY}      # From environment variable
  secret-key: ${IYZICO_SECRET_KEY} # From environment variable
  base-url: https://sandbox-api.iyzipay.com  # Sandbox for dev
```

> ⚠️ **Note:** Currently configured for **Iyzico Sandbox**. For production, change `base-url` to `https://api.iyzipay.com` and use production credentials.

---

## 🧪 Testing

### Unit Tests (JUnit 5 + Mockito)

Service layer tests for each microservice:

```bash
# Run all tests
./mvnw test

# Run tests for a specific service
cd backend/auth-service && ./mvnw test
```

Test coverage targets:
- ✅ AuthService — register, login, token validation
- ✅ ProductService — CRUD, pagination, search
- ✅ CartService — add, remove, update, clear
- ✅ OrderService — create, status transitions

### Integration Tests (@SpringBootTest)

```bash
# Requires Docker for TestContainers (PostgreSQL)
./mvnw verify -P integration-tests
```

### Frontend Tests (Vitest)

```bash
cd frontend
npm run test
```

---

## 🌍 Environment Variables

Create a `.env` file in the project root:

```env
# ─── Database ───────────────────────────────
POSTGRES_USER=ecommerce_user
POSTGRES_PASSWORD=your_secure_password
POSTGRES_DB=ecommerce

# ─── JWT ────────────────────────────────────
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_at_least_64_chars

# ─── Redis ──────────────────────────────────
REDIS_HOST=redis
REDIS_PORT=6379

# ─── Iyzico ─────────────────────────────────
IYZICO_API_KEY=your_iyzico_api_key        # TODO: Add real credentials
IYZICO_SECRET_KEY=your_iyzico_secret_key  # TODO: Add real credentials

# ─── Frontend ───────────────────────────────
VITE_AUTH_SERVICE_URL=http://localhost:8081
VITE_PRODUCT_SERVICE_URL=http://localhost:8082
VITE_CART_SERVICE_URL=http://localhost:8083
VITE_ORDER_SERVICE_URL=http://localhost:8084
```

> ⚠️ **Never commit `.env` to git.** It's already in `.gitignore`.

---

## 📊 Coding Standards

This project follows **Clean Code** and **SOLID principles**:

- **DTOs everywhere** — Entities are never exposed directly in API responses
- **Global Exception Handling** — `@ControllerAdvice` with consistent error format
- **@Slf4j Logging** — Structured logging in every service layer class
- **@Valid Validation** — Bean Validation annotations on all request DTOs
- **Javadoc** — All public service methods are documented
- **application.yml** — YAML config over `.properties` for readability
- **No hardcoded secrets** — All sensitive values from environment variables

---

## 🏆 What Makes This Stand Out

| Feature | Standard | This Project |
|---|---|---|
| Architecture | Monolith | **Microservices** |
| Cart Storage | DB only | **Redis** (high-performance) |
| API Docs | Optional | **Swagger on every service** |
| CI/CD | Manual deploy | **Full GitHub Actions pipeline** |
| Containerization | Basic Docker | **Multi-stage builds + Docker Compose** |
| Security | Session-based | **Stateless JWT** |

---

## 📬 Author

Developed as the final project for **N11 TalentHub Java & React Bootcamp**.

---

<div align="center">

Made with ☕ Java + ⚛️ React for N11 TalentHub Bootcamp

</div>
