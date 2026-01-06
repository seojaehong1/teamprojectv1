# CLAUDE.md - Project Guide for AI Assistant

## Project Overview
MSA (Microservices Architecture) based coffee shop application built with Spring Boot 3.1.5 and Spring Cloud 2022.0.4.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.1.5, Spring Cloud 2022.0.4
- **Build Tool**: Gradle
- **Database**: H2 (in-memory)
- **Message Queue**: RabbitMQ
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Template Engine**: Thymeleaf (frontend-service)
- **Authentication**: JWT, OAuth2 (Google, Naver)

## Project Structure
```
teamprojectv1/
├── eureka-server/       # Service Discovery (port 8761)
├── gateway-service/     # API Gateway (port 8000)
├── frontend-service/    # Web UI with Thymeleaf (port 8005)
├── member-service/      # User authentication & management (port 8004)
├── product-service/     # Product catalog (port 8001)
├── order-service/       # Order management (port 8002)
├── inventory-service/   # Inventory management
├── board-service/       # Board/Notice/FAQ management
├── cust-service/        # Customer service
└── admin-service/       # Admin dashboard
```

## Service Ports
| Service | Port |
|---------|------|
| eureka-server | 8761 |
| gateway-service | 8000 |
| product-service | 8001 |
| order-service | 8002 |
| member-service | 8004 |
| frontend-service | 8005 (메인 진입점) |
| board-service | 8006 |
| admin-service | 8007 |
| inventory-service | 8008 |

## API Routes

### Frontend-service 프록시 (권장 - localhost:8005)
Frontend-service에서 각 서비스로 API 요청을 프록시합니다:
- `/api/auth/**` -> member-service (8004)
- `/api/users/**` -> member-service (8004)
- `/api/notices/**` -> board-service (8006)
- `/api/boards/**` -> board-service (8006)
- `/api/comments/**` -> board-service (8006)
- `/api/admin/**` -> admin-service (8007)
- `/api/inquiries/**` -> admin-service (8007)
- `/api/products/**` -> product-service (8002)
- `/api/orders/**` -> order-service (8003)
- `/api/inventory/**` -> inventory-service (8008)

### Gateway 라우팅 (localhost:8000)
- `/api/products/**` -> product-service
- `/api/orders/**` -> order-service
- `/api/customers/**` -> cust-service
- `/api/messages/**` -> cust-service
- `/api/auth/**` -> member-service
- `/api/admin/**` -> member-service
- `/api/boards/**` -> board-service
- `/api/comments/**` -> board-service
- `/api/inventory/**` -> inventory-service
- `/**` -> frontend-service (fallback)

## Build & Run Commands
```bash
# Build all services
./gradlew build

# Run individual service
./gradlew :eureka-server:bootRun
./gradlew :gateway-service:bootRun
./gradlew :product-service:bootRun
./gradlew :member-service:bootRun
./gradlew :frontend-service:bootRun

# Clean build
./gradlew clean build
```

## Startup Order
1. eureka-server (must start first)
2. Other backend services (product, order, member, board, inventory, cust, admin)
3. gateway-service
4. frontend-service

## Environment Variables
Required environment variables for member-service:
- `MAIL_USERNAME` - Gmail SMTP username
- `MAIL_PASSWORD` - Gmail app password
- `GOOGLE_CLIENT_ID` - Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth2 client secret
- `NAVER_CLIENT_ID` - Naver OAuth2 client ID
- `NAVER_CLIENT_SECRET` - Naver OAuth2 client secret

## Git Workflow
- Main branch: `main`
- Development branch: `develop`
- Feature branches: `feature/*`

## Key Patterns
- Each service has its own H2 in-memory database
- Services communicate via REST through the gateway
- JWT-based authentication with interceptors
- Thymeleaf templates in `frontend-service/src/main/resources/templates/`
- Static resources in respective service's `src/main/resources/static/`

## Code Conventions
- Package naming: `com.example.<service-name>` or `com.du.<service-name>`
- Standard Spring Boot project structure (controller, service, repository, model)
- Korean comments are common in this codebase
