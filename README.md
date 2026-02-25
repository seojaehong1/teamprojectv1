# TORI Coffee - MSA 기반 주문 관리 시스템

Spring Cloud 기반 마이크로서비스 아키텍처(MSA)로 구현한 커피숍 주문/재고 관리 시스템

---

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.1.5, Spring Cloud 2022.0.4 |
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway (Reactive) |
| **Message Queue** | RabbitMQ 3.12 (Topic Exchange) |
| **Database** | MySQL 8.0 (서비스별 독립 DB 4개) |
| **Authentication** | JWT (HS512), OAuth2 (Google, Naver) |
| **Container** | Docker, Kubernetes (AWS EKS) |
| **Build** | Gradle 8.x (Multi-module) |

---

## System Architecture

```
                                    ┌─────────────────┐
                                    │     Client      │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │    AWS ALB      │
                                    └────────┬────────┘
                                             │
┌────────────────────────────────────────────▼────────────────────────────────────────────┐
│                              Spring Cloud Gateway (8000)                                 │
│                           • Path-based Routing (17 routes)                              │
│                           • Client-side Load Balancing (lb://)                          │
│                           • CORS Configuration                                          │
└────────────────────────────────────────────┬────────────────────────────────────────────┘
                                             │
              ┌──────────────┬───────────────┼───────────────┬──────────────┐
              ▼              ▼               ▼               ▼              ▼
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
    │    Member    │ │   Product    │ │    Order     │ │  Inventory   │ │    Board     │
    │   Service    │ │   Service    │ │   Service    │ │   Service    │ │   Service    │
    │    (8004)    │ │    (8001)    │ │    (8002)    │ │    (8008)    │ │    (8006)    │
    └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
           │                │                │                │                │
           │                │                │   RabbitMQ     │                │
           │                │                │◄══════════════►│                │
           │                │                │  (Async Event) │                │
           ▼                ▼                ▼                ▼                ▼
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
    │  member_db   │ │  product_db  │ │   order_db   │ │ inventory_db │        │
    │    (3306)    │ │    (3307)    │ │    (3308)    │ │    (3309)    │◄───────┘
    └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
                                             ▲
                              ┌──────────────┴──────────────┐
                              │      Eureka Server (8761)   │
                              │      Service Registry       │
                              └─────────────────────────────┘
```

---

## Microservices

| Service | Port | Responsibility | Database |
|---------|------|----------------|----------|
| `eureka-server` | 8761 | 서비스 등록/발견, Health Check | - |
| `gateway-service` | 8000 | API 라우팅, 로드밸런싱, CORS | - |
| `frontend-service` | 8005 | Thymeleaf 웹 UI | - |
| `member-service` | 8004 | 인증, JWT, OAuth2 | member_db |
| `product-service` | 8001 | 상품/카테고리 CRUD | product_db |
| `order-service` | 8002 | 주문, 장바구니 | order_db |
| `inventory-service` | 8008 | 재고 관리 | inventory_db |
| `board-service` | 8006 | 게시판, 공지사항 | member_db (공유) |
| `admin-service` | 8007 | 관리자 대시보드 | member_db (공유) |

---

## Core Implementation

### 1. Event-Driven Architecture (비동기 메시징)

주문 생성과 재고 차감을 RabbitMQ 기반 비동기 통신으로 분리하여 서비스 간 결합도 최소화

```
Order Service                           Inventory Service
      │                                        │
      │ 1. 주문 저장 (Transactional)           │
      │                                        │
      │ 2. 메시지 발행 ────────────────────────►│
      │    Exchange: order.exchange            │
      │    Routing Key: order.placed           │
      │                                        │ 3. 메시지 수신
      │ 3. 즉시 응답 반환                       │    @RabbitListener
      │                                        │
      ▼                                        │ 4. 레시피 기반 재고 차감
   [Client]                                    ▼
```

**Producer - OrderService.java**
```java
@Transactional
public OrderResponse createOrder(OrderRequest request) {
    Order order = orderRepository.save(buildOrder(request));

    // 비동기 메시지 발행
    rabbitTemplate.convertAndSend(
        "order.exchange",
        "order.placed",
        OrderStockMessage.from(order)
    );

    return OrderResponse.from(order);
}
```

**Consumer - OrderMessageListener.java**
```java
@RabbitListener(queues = "inventory.order.queue")
public void handleOrderPlaced(OrderStockRequestDto request) {
    inventoryService.processOrderStock(request);
}
```

### 2. Database per Service

서비스별 독립 데이터베이스로 **장애 격리** 및 **독립적 스케일링**

| Database | Port | Services | Key Tables |
|----------|------|----------|------------|
| member_db | 3306 | member, board, admin | users, inquiry, notice |
| product_db | 3307 | product | product, category, option |
| order_db | 3308 | order | orders, order_items, cart |
| inventory_db | 3309 | inventory | material_master, recipe |

### 3. JWT Authentication

**Dual Token Strategy**

| Token | Algorithm | Expiry | Purpose |
|-------|-----------|--------|---------|
| Access Token | HS512 | 1 hour | API 인증 |
| Refresh Token | HS512 | 7 days | 토큰 갱신 |

**Security Features**
- 로그인 5회 실패 시 30분 계정 잠금
- OAuth2 소셜 로그인 (Google, Naver)
- 서비스별 JWT Interceptor 검증

### 4. Service Discovery & Gateway

**Eureka 기반 동적 서비스 등록**
- 30초 간격 Heartbeat
- 서비스 시작/종료 시 자동 등록/해제

**Gateway 라우팅**
```properties
spring.cloud.gateway.routes[0].id=product-service
spring.cloud.gateway.routes[0].uri=lb://product-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/products/**
```

---

## Kubernetes Configuration

### HPA (Horizontal Pod Autoscaler)

| Service | Min | Max | CPU Target | Memory Target |
|---------|-----|-----|------------|---------------|
| gateway | 2 | 10 | 70% | 80% |
| order | 2 | 10 | 70% | 80% |
| product | 2 | 8 | 70% | 80% |
| inventory | 2 | 8 | 70% | 80% |
| member | 2 | 8 | 70% | 80% |

**Scaling Policy**
```yaml
behavior:
  scaleUp:
    stabilizationWindowSeconds: 0      # 즉시 확장
    policies:
      - type: Percent
        value: 100
        periodSeconds: 15
  scaleDown:
    stabilizationWindowSeconds: 300    # 5분 안정화 후 축소
```

### Resource Management
- **ConfigMap**: Eureka URL, RabbitMQ Host, Service URLs
- **Secrets**: DB Passwords, JWT Secret, OAuth Credentials

---

## Project Structure

```
teamprojectv1/
├── eureka-server/              # Service Discovery
├── gateway-service/            # API Gateway
├── frontend-service/           # Web UI (Thymeleaf)
├── member-service/             # Authentication
├── product-service/            # Product Catalog
├── order-service/              # Order Management (RabbitMQ Producer)
├── inventory-service/          # Stock Management (RabbitMQ Consumer)
├── board-service/              # Community
├── admin-service/              # Admin Dashboard
├── k8s/
│   ├── base/                   # ConfigMap, Secrets
│   ├── hpa/                    # HPA Configurations
│   ├── services/               # Deployment YAMLs
│   └── mysql/                  # Database StatefulSets
├── docker-compose.yml
├── API_DOCUMENTATION.md
└── API-MAPPING.md
```

---

## Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- RabbitMQ

### Local Development

```bash
# 1. Infrastructure
docker-compose up -d mysql-member mysql-product mysql-order mysql-inventory rabbitmq

# 2. Services (순차 실행)
./gradlew :eureka-server:bootRun
./gradlew :gateway-service:bootRun
./gradlew :member-service:bootRun
./gradlew :product-service:bootRun
./gradlew :order-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :frontend-service:bootRun
```

### Access Points
| Service | URL |
|---------|-----|
| Gateway | http://localhost:8000 |
| Frontend | http://localhost:8005 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 |

---

## API Reference

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | JWT 발급 |
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/refresh` | 토큰 갱신 |

### Orders (Async Processing)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/order` | 주문 생성 → RabbitMQ 발행 |
| GET | `/api/order/{id}` | 주문 조회 |

### Inventory
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/inventory` | 재고 현황 |
| PUT | `/api/owner/inventory/{id}` | 재고 수정 |

---

## Documentation

| Document | Description |
|----------|-------------|
| [API Documentation](./API_DOCUMENTATION.md) | 전체 API 레퍼런스 |
| [API Mapping](./API-MAPPING.md) | Frontend-Backend 매핑 |
| [K8s Deployment](./k8s/README.md) | Kubernetes 배포 가이드 |

---

## Technical Decisions

| Decision | Rationale |
|----------|-----------|
| **Eureka** | Spring Cloud 생태계 통합, 커스텀 헬스체크 |
| **RabbitMQ** | 트랜잭션 메시지 보장, Topic Exchange 라우팅 |
| **DB per Service** | 장애 격리, 독립적 스케일링 |
| **JWT Stateless** | 수평 확장 용이, 서버 세션 불필요 |

---

## Team

- **Backend Architect**: 서재홍 (팀장)
- **Team Size**: 4명
- **Duration**: 3주

---

## Demo

**시연 영상 및 상세 설명**: [https://jaehong-dev.com/projects/msa](https://jaehong-dev.com/projects/msa)