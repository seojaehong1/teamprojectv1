# 프로젝트 아키텍처 문서

## 1. 전체 디렉토리 구조와 각 디렉토리/모듈의 역할

```
teamprojectv1/
├── .github/workflows/          # CI/CD 파이프라인 정의
│   └── ci.yml                  # GitHub Actions: 9개 서비스 병렬 빌드 및 ECR 푸시
├── k8s/                        # Kubernetes 배포 매니페스트
│   ├── base/                   # 기본 K8s 리소스 (namespace, configmap, secrets)
│   ├── services/               # 9개 마이크로서비스 Deployment/Service 정의
│   ├── mysql/                  # 4개 MySQL StatefulSet 정의
│   ├── rabbitmq/               # RabbitMQ Deployment/Service 정의
│   ├── hpa/                    # Horizontal Pod Autoscaler 설정
│   ├── ingress.yaml            # ALB Ingress Controller 설정
│   └── argocd/                 # ArgoCD GitOps 배포 설정
├── gateway-service/            # API Gateway (Spring Cloud Gateway)
│   └── src/main/resources/application.properties  # 17개 라우트 설정
├── eureka-server/              # 서비스 디스커버리 레지스트리
├── member-service/             # 회원 관리 서비스
│   ├── Controller: AuthController, UserController, AdminController
│   └── src/main/resources/application.properties  # JWT, OAuth2, Mail 설정
├── product-service/            # 상품/메뉴 관리 서비스
│   └── Controller: MenuController, OptionController, Admin 컨트롤러 5개
├── order-service/              # 주문 관리 서비스
│   ├── Controller: OrderController, AdminController
│   └── RabbitMQ Publisher (order.exchange)
├── inventory-service/          # 재고 관리 서비스
│   ├── Controller: InventoryController, MaterialController, OwnerController
│   └── RabbitMQ Consumer (inventory.order.queue)
├── board-service/              # 게시판/커뮤니티 서비스
│   └── Controller: BoardController, CommentController, NoticeController, EventController
├── admin-service/              # 관리자 전용 서비스
│   └── Controller 6개
├── frontend-service/           # 웹 프론트엔드 (Thymeleaf SSR)
│   └── Controller: ApiProxyController, MainController 등 7개
├── docker-compose.yml          # 로컬 개발 환경 (9 서비스 + 4 MySQL + RabbitMQ)
├── build.gradle                # 루트 빌드 설정 (Spring Boot 3.1.5)
└── settings.gradle             # 멀티모듈 프로젝트 설정 (9개 서비스)
```

**각 서비스별 역할:**

| 서비스 | 포트 | 역할 | 주요 Controller |
|--------|------|------|----------------|
| `eureka-server` | 8761 | 서비스 디스커버리 레지스트리 | - |
| `gateway-service` | 8000 | API Gateway, 라우팅, 인증 필터 | - |
| `product-service` | 8001 | 메뉴/옵션 관리, 상품 CRUD | MenuController, OptionController |
| `order-service` | 8002 | 주문 생성/조회, RabbitMQ 발행 | OrderController |
| `inventory-service` | 8003 | 재고 관리, RabbitMQ 소비 | InventoryController, MaterialController |
| `member-service` | 8004 | 회원가입, 로그인, JWT 발급, OAuth2 | AuthController, UserController |
| `board-service` | 8005 | 게시글, 댓글, 공지사항, 이벤트 | BoardController, CommentController, NoticeController |
| `frontend-service` | 8100 | 웹 UI (Thymeleaf SSR) | ApiProxyController, MainController |
| `admin-service` | 8200 | 관리자 전용 기능 | AdminController 등 6개 |

---

## 2. 사용 중인 기술 스택 (버전 포함)

### 백엔드 프레임워크 및 언어
| 기술 | 버전 | 출처 |
|------|------|------|
| **Java** | 17 | `build.gradle:4` - `sourceCompatibility = '17'` |
| **Spring Boot** | 3.1.5 | `build.gradle:10` - `id 'org.springframework.boot' version '3.1.5'` |
| **Spring Cloud** | 2022.0.4 | `build.gradle:33` - `springCloudVersion = '2022.0.4'` |
| **Gradle** | 8.x | `docker-compose.yml`, Dockerfile에서 `gradle:8-jdk17` 이미지 사용 |

### Spring Cloud 컴포넌트
| 컴포넌트 | 용도 | 확인 위치 |
|----------|------|----------|
| **Spring Cloud Gateway** | API Gateway (WebFlux 기반) | `gateway-service/build.gradle` |
| **Netflix Eureka** | 서비스 디스커버리 | `eureka-server/`, 모든 서비스 `application.properties` |
| **Spring Cloud Config** (확인 필요) | 중앙화된 설정 관리 | 확인 필요 - ConfigMap 사용 중 |

### 데이터베이스
| 기술 | 버전 | 포트 | 출처 |
|------|------|------|------|
| **MySQL** | 8.0 | 3306, 3307, 3308, 3309 | `docker-compose.yml:59-107`, `k8s/mysql/*.yaml` |

**데이터베이스 목록:**
- `member_db` (3306): member-service, board-service, admin-service 공유
- `product_db` (3307): product-service 전용
- `order_db` (3308): order-service 전용
- `inventory_db` (3309): inventory-service 전용

### ORM 및 데이터 접근
| 기술 | 용도 | 출처 |
|------|------|------|
| **Spring Data JPA** | JPA 구현체 | 모든 서비스 `build.gradle` |
| **Hibernate** | ORM 프레임워크 | Spring Boot 기본 포함 |
| **HikariCP** | 커넥션 풀 | `application.properties` - `spring.datasource.hikari.maximum-pool-size=50` |

### 메시지 큐
| 기술 | 버전 | 포트 | 출처 |
|------|------|------|------|
| **RabbitMQ** | 3-management-alpine | 5672 (AMQP), 15672 (Management UI) | `docker-compose.yml:109-125`, `k8s/rabbitmq/rabbitmq.yaml` |

### 인증 및 보안
| 기술 | 설정값 | 출처 |
|------|--------|------|
| **JWT** | HS512 알고리즘 | `member-service/application.properties:22` |
| **Access Token** | 1시간 (3600000ms) | `jwt.expiration=3600000` |
| **Refresh Token** | 7일 (604800000ms) | `jwt.refresh-token.expiration=604800000` |
| **OAuth2 Social Login** | Google, Naver | `member-service/application.properties:29-38` |

### 템플릿 엔진
| 기술 | 용도 | 출처 |
|------|------|------|
| **Thymeleaf** | SSR 웹 페이지 렌더링 | `frontend-service/build.gradle`, `application.properties:14` |

### 인프라 및 배포
| 기술 | 버전/설정 | 출처 |
|------|----------|------|
| **Docker** | Multi-stage builds (Gradle 8 + Eclipse Temurin 17 JRE Alpine) | Dockerfile in each service |
| **Docker Compose** | 로컬 개발 환경 | `docker-compose.yml` |
| **Kubernetes** | AWS EKS | `k8s/` 디렉토리 전체 |
| **ArgoCD** | GitOps 배포, auto-sync 활성화 | `k8s/argocd/root-app.yaml` |
| **AWS ECR** | 컨테이너 레지스트리 | `.github/workflows/ci.yml:37-45` |
| **GitHub Actions** | CI/CD 파이프라인 (9개 서비스 병렬 빌드) | `.github/workflows/ci.yml` |

### 모니터링
| 기술 | 용도 | 출처 |
|------|------|------|
| **Spring Boot Actuator** | Health check, Metrics | 모든 서비스 `build.gradle` |
| **Micrometer Prometheus** | 메트릭 수집 | `build.gradle` - `runtimeOnly 'io.micrometer:micrometer-registry-prometheus'` |

### 이메일
| 기술 | 설정 | 출처 |
|------|------|------|
| **Gmail SMTP** | TLS 587포트 | `member-service/application.properties:40-45` |

---

## 3. 서비스/모듈 목록과 각각의 책임

### Core Infrastructure Services

#### 1. eureka-server
**책임**: 마이크로서비스 디스커버리 및 레지스트리
- 모든 서비스 인스턴스 등록 및 조회
- 헬스체크를 통한 서비스 상태 관리
- 클라이언트측 로드밸런싱 지원

**설정**: `application.properties`
```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### 2. gateway-service
**책임**: API Gateway, 중앙 진입점
- 17개 라우트 규칙으로 요청 라우팅
- JWT 인증 필터 적용
- CORS 정책 관리 (모든 오리진 허용)
- Eureka를 통한 동적 라우팅 (`lb://service-name`)

**주요 라우트 예시** (`application.properties`):
```properties
# Member Service
spring.cloud.gateway.routes[0].id=member-service
spring.cloud.gateway.routes[0].uri=lb://member-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**,/api/users/**

# Product Service
spring.cloud.gateway.routes[2].id=product-service
spring.cloud.gateway.routes[2].uri=lb://product-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/menu/**,/api/options/**
```

### Business Domain Services

#### 3. member-service
**책임**: 회원 관리, 인증/인가
- 회원가입, 로그인, 로그아웃
- JWT Access Token 및 Refresh Token 발급
- OAuth2 소셜 로그인 (Google, Naver)
- 비밀번호 재설정 (이메일 인증)
- 관리자 회원 관리

**주요 Controller**:
- `AuthController`: 로그인, 회원가입, 토큰 갱신
- `UserController`: 회원정보 조회/수정, 비밀번호 변경
- `AdminController`: 관리자용 회원 관리

**데이터베이스**: `member_db` (board-service, admin-service와 공유)

#### 4. product-service
**책임**: 상품/메뉴 관리
- 메뉴 등록, 수정, 삭제, 조회
- 메뉴 옵션 관리
- 카테고리 관리
- 관리자용 상품 통계 및 분석

**주요 Controller**:
- `MenuController`: 메뉴 CRUD
- `OptionController`: 옵션 CRUD
- Admin 컨트롤러 5개: 상품 관리 및 통계

**데이터베이스**: `product_db` (전용)

#### 5. order-service
**책임**: 주문 관리, 주문 이벤트 발행
- 주문 생성, 조회, 취소
- 주문 상태 관리
- **RabbitMQ를 통한 재고 차감 이벤트 발행**

**주요 Controller**:
- `OrderController`: 주문 CRUD
- `AdminController`: 관리자용 주문 관리

**RabbitMQ 발행** (`OrderService.java:105`):
```java
rabbitTemplate.convertAndSend("order.exchange", "order.placed", orderMessage);
```

**데이터베이스**: `order_db` (전용)

#### 6. inventory-service
**책임**: 재고 관리, 주문 이벤트 소비
- 재고 수량 관리
- 입고/출고 처리
- 자재 관리
- **RabbitMQ를 통한 주문 이벤트 수신 및 재고 차감**

**주요 Controller**:
- `InventoryController`: 재고 조회/수정
- `MaterialController`: 자재 관리
- `OwnerController`: 오너용 재고 관리

**RabbitMQ 소비** (`OrderMessageListener.java:15-18`):
```java
@RabbitListener(queues = "inventory.order.queue")
public void handleOrderPlaced(OrderPlacedEvent event) {
    // 재고 차감 로직
}
```

**데이터베이스**: `inventory_db` (전용)

#### 7. board-service
**책임**: 커뮤니티 및 게시판 관리
- 게시글 작성, 수정, 삭제, 조회
- 댓글 관리
- 공지사항 관리
- 이벤트 관리

**주요 Controller**:
- `BoardController`: 게시글 CRUD
- `CommentController`: 댓글 CRUD
- `NoticeController`: 공지사항 관리
- `EventController`: 이벤트 관리

**데이터베이스**: `member_db` (member-service, admin-service와 공유)

#### 8. frontend-service
**책임**: 웹 UI 렌더링 (SSR)
- Thymeleaf 템플릿 기반 서버사이드 렌더링
- API Gateway를 통한 백엔드 서비스 호출
- 정적 리소스 제공

**주요 Controller**:
- `ApiProxyController`: 백엔드 API 프록시
- `MainController`: 메인 페이지
- 기타 5개 Controller

**템플릿 경로**: `src/main/resources/templates/`

#### 9. admin-service
**책임**: 관리자 전용 기능
- 시스템 관리
- 통계 및 대시보드
- 관리자 권한 관리

**주요 Controller**: 6개 (구체적 기능은 Controller 코드 분석 필요)

**데이터베이스**: `member_db` (member-service, board-service와 공유)

---

## 4. 서비스/모듈 간 통신 방식

### 4.1 동기 통신 (REST API)

#### API Gateway를 통한 라우팅
모든 외부 요청은 `gateway-service:8000`을 통해 라우팅됨.

**통신 흐름**:
```
Client → Gateway (8000) → Eureka 조회 → Target Service
```

**라우팅 예시** (`gateway-service/application.properties`):
```properties
# Order Service 라우팅
spring.cloud.gateway.routes[4].id=order-service
spring.cloud.gateway.routes[4].uri=lb://order-service
spring.cloud.gateway.routes[4].predicates[0]=Path=/api/orders/**

# Inventory Service 라우팅
spring.cloud.gateway.routes[6].id=inventory-service
spring.cloud.gateway.routes[6].uri=lb://inventory-service
spring.cloud.gateway.routes[6].predicates[0]=Path=/api/inventory/**
```

**로드 밸런싱**:
- `lb://service-name` 프로토콜 사용
- Eureka에서 서비스 인스턴스 목록 조회
- 클라이언트측 로드밸런싱 (Ribbon/LoadBalancer)

#### 서비스 간 직접 호출 (확인 필요)
Frontend-service가 API Gateway를 통해 백엔드 서비스 호출하는지, 아니면 Eureka를 통해 직접 호출하는지 확인 필요.

### 4.2 비동기 통신 (Event-Driven)

#### RabbitMQ 메시징

**주문 → 재고 이벤트 흐름**:
```
order-service (Publisher)
  ↓
order.exchange (Exchange)
  ↓ routing key: "order.placed"
inventory.order.queue (Queue)
  ↓
inventory-service (Consumer)
```

**발행자** (`order-service`):
- 파일: `OrderService.java:105`
- Exchange: `order.exchange`
- Routing Key: `order.placed`
- 메시지: `OrderPlacedEvent` (주문 정보 포함)

**소비자** (`inventory-service`):
- 파일: `OrderMessageListener.java:15-18`
- Queue: `inventory.order.queue`
- Listener: `@RabbitListener` 어노테이션 사용
- 처리: 재고 차감 로직 실행

**RabbitMQ 연결 설정** (예: `order-service/application.properties:18-19`):
```properties
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
```

**기타 메시지 큐 사용 (확인 필요)**:
- product-service, board-service 등 다른 서비스의 RabbitMQ 사용 여부 확인 필요

### 4.3 서비스 디스커버리 (Eureka)

**등록 과정**:
1. 각 서비스 시작 시 Eureka Server에 등록
2. 주기적으로 heartbeat 전송 (기본 30초)
3. Eureka Server가 서비스 인스턴스 목록 유지

**설정 예시** (`member-service/application.properties:3`):
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**서비스 이름**:
```properties
spring.application.name=member-service  # 각 서비스별 고유 이름
```

### 4.4 인증/인가 흐름

**JWT 기반 인증**:
```
1. Client → Gateway → member-service/api/auth/login
2. member-service: JWT Access Token + Refresh Token 발급
3. Client → Gateway (Authorization: Bearer {token})
4. Gateway: JWT 검증 필터 적용
5. Gateway → Target Service (인증된 요청 전달)
```

**OAuth2 소셜 로그인**:
```
1. Client → Gateway → member-service/oauth2/authorization/{google|naver}
2. member-service: OAuth2 Provider로 리다이렉트
3. Provider: 인증 후 Callback URL로 리다이렉트
4. member-service: JWT 발급 및 반환
```

---

## 5. 설정 파일 위치와 주요 설정값

### 5.1 빌드 설정

#### `build.gradle` (루트)
```gradle
# 공통 설정
sourceCompatibility = '17'
springBootVersion = '3.1.5'
springCloudVersion = '2022.0.4'

# 공통 의존성
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    runtimeOnly 'com.mysql:mysql-connector-j'
    runtimeOnly 'io.micrometer:micrometer-registry-prometheus'
}
```

#### `settings.gradle`
```gradle
rootProject.name = 'MSAproject_park'
include 'gateway-service', 'eureka-server', 'product-service', 'order-service',
        'member-service', 'board-service', 'inventory-service',
        'frontend-service', 'admin-service'
```

### 5.2 서비스별 application.properties

#### eureka-server (`eureka-server/src/main/resources/application.properties`)
```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### gateway-service (`gateway-service/src/main/resources/application.properties`)
**주요 설정**:
- 포트: `8000`
- 라우트: 17개 정의 (각 서비스별 경로 매핑)
- CORS: 모든 오리진 허용
- Eureka 연결: `http://localhost:8761/eureka/`

**라우트 패턴**:
```properties
spring.cloud.gateway.routes[N].id=서비스명
spring.cloud.gateway.routes[N].uri=lb://서비스명
spring.cloud.gateway.routes[N].predicates[0]=Path=/api/경로/**
```

#### member-service (`member-service/src/main/resources/application.properties`)
**주요 설정**:
```properties
server.port=8004
spring.application.name=member-service

# Database
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/member_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# HikariCP
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-32-characters-long}
jwt.expiration=${JWT_EXPIRATION:3600000}
jwt.refresh-token.expiration=604800000

# OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.naver.client-id=${NAVER_CLIENT_ID}
spring.security.oauth2.client.registration.naver.client-secret=${NAVER_CLIENT_SECRET}

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

#### order-service (`order-service/src/main/resources/application.properties`)
**주요 설정**:
```properties
server.port=8002
spring.application.name=order-service

# Database
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3308/order_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8}

# RabbitMQ
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

#### 기타 서비스 공통 패턴
- `product-service`: 8001 포트, `product_db:3307`
- `inventory-service`: 8003 포트, `inventory_db:3309`, RabbitMQ consumer
- `board-service`: 8005 포트, `member_db:3306` 공유
- `frontend-service`: 8100 포트, Thymeleaf 설정
- `admin-service`: 8200 포트, `member_db:3306` 공유

### 5.3 Docker 설정

#### `docker-compose.yml`
**주요 구성**:
```yaml
version: '3.8'
networks:
  tori-network:
    driver: bridge

services:
  # MySQL 4개 인스턴스
  mysql-member:
    image: mysql:8.0
    ports: ["3306:3306"]
    environment:
      MYSQL_DATABASE: member_db
      MYSQL_ROOT_PASSWORD: root

  mysql-product:
    ports: ["3307:3306"]
    environment:
      MYSQL_DATABASE: product_db

  mysql-order:
    ports: ["3308:3306"]
    environment:
      MYSQL_DATABASE: order_db

  mysql-inventory:
    ports: ["3309:3306"]
    environment:
      MYSQL_DATABASE: inventory_db

  # RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

  # 9개 마이크로서비스
  eureka-server:
    build: ./eureka-server
    ports: ["8761:8761"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  gateway-service:
    depends_on: [eureka-server]
    ports: ["8000:8000"]

  # ... (나머지 서비스들)
```

### 5.4 Kubernetes 설정

#### ConfigMap (`k8s/base/configmap.yaml`)
**주요 설정**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: tori
data:
  EUREKA_SERVER_URL: "http://eureka-server:8761/eureka/"
  RABBITMQ_HOST: "rabbitmq"
  RABBITMQ_PORT: "5672"
  # 각 서비스별 데이터베이스 URL
  MEMBER_DB_URL: "jdbc:mysql://mysql-member:3306/member_db?..."
  PRODUCT_DB_URL: "jdbc:mysql://mysql-product:3306/product_db?..."
  ORDER_DB_URL: "jdbc:mysql://mysql-order:3306/order_db?..."
  INVENTORY_DB_URL: "jdbc:mysql://mysql-inventory:3306/inventory_db?..."
```

#### Secrets (`k8s/base/secrets.yaml`)
**주요 설정** (Base64 인코딩됨):
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: tori
type: Opaque
data:
  DB_USERNAME: <base64>
  DB_PASSWORD: <base64>
  JWT_SECRET: <base64>
  GOOGLE_CLIENT_ID: <base64>
  GOOGLE_CLIENT_SECRET: <base64>
  NAVER_CLIENT_ID: <base64>
  NAVER_CLIENT_SECRET: <base64>
  MAIL_USERNAME: <base64>
  MAIL_PASSWORD: <base64>
```

#### HPA (`k8s/hpa/hpa.yaml`)
**주요 설정**:
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: gateway-service-hpa
  namespace: tori
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: gateway-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 0  # 즉시 스케일업
    scaleDown:
      stabilizationWindowSeconds: 300  # 5분 안정화
```

**HPA 적용 서비스**: gateway, member, product, order, inventory, board, admin (7개)

### 5.5 CI/CD 설정

#### GitHub Actions (`.github/workflows/ci.yml`)
**주요 설정**:
```yaml
name: Build and Push to ECR
on:
  push:
    branches: [main]

jobs:
  build:
    strategy:
      matrix:
        service:
          - gateway-service
          - eureka-server
          - member-service
          - product-service
          - order-service
          - inventory-service
          - board-service
          - frontend-service
          - admin-service

    steps:
      - uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-region: ap-northeast-2

      - name: Login to ECR
        run: aws ecr get-login-password | docker login --username AWS --password-stdin <ECR_URI>

      - name: Create ECR repository if not exists
        run: |
          aws ecr describe-repositories --repository-names ${{ matrix.service }} || \
          aws ecr create-repository --repository-name ${{ matrix.service }}

      - name: Build and Push Docker image
        run: |
          docker build -t ${{ matrix.service }}:latest ./${{ matrix.service }}
          docker tag ${{ matrix.service }}:latest <ECR_URI>/${{ matrix.service }}:latest
          docker push <ECR_URI>/${{ matrix.service }}:latest
```

#### ArgoCD (`k8s/argocd/root-app.yaml`)
**주요 설정**:
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: tori-root
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/<user>/teamprojectv1
    targetRevision: main
    path: k8s
  destination:
    server: https://kubernetes.default.svc
    namespace: tori
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
      - CreateNamespace=true
```

---

## 6. 빌드 및 실행 방법

### 6.1 로컬 개발 환경 (Docker Compose)

#### 사전 요구사항
- Docker Desktop 설치
- Java 17 JDK 설치
- Gradle 8.x 설치

#### 전체 빌드
```bash
# 루트 디렉토리에서 모든 서비스 빌드
./gradlew clean build

# 특정 서비스만 빌드
./gradlew :member-service:build
```

#### Docker Compose 실행
```bash
# 모든 서비스 시작 (빌드 포함)
docker-compose up --build

# 백그라운드 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f [service-name]

# 중지
docker-compose down

# 볼륨까지 삭제 (데이터베이스 초기화)
docker-compose down -v
```

#### 서비스 시작 순서
Docker Compose의 `depends_on`과 `healthcheck`를 통해 자동 관리됨:
```
1. MySQL 4개 인스턴스 (병렬)
2. RabbitMQ
3. eureka-server (healthcheck 대기)
4. gateway-service (eureka-server 의존)
5. 나머지 7개 서비스 (eureka-server 의존)
```

#### 접속 URL
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8000
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- Frontend: http://localhost:8100
- 각 서비스 직접 접속: http://localhost:800X

### 6.2 개별 서비스 실행 (로컬)

#### IDE에서 실행 (IntelliJ/Eclipse)
1. 각 서비스의 `Application.java` 실행
2. 실행 순서: eureka-server → gateway-service → 기타 서비스

#### 커맨드라인 실행
```bash
# Eureka Server 먼저 실행
cd eureka-server
./gradlew bootRun

# Gateway 실행 (새 터미널)
cd gateway-service
./gradlew bootRun

# Member Service 실행 (새 터미널)
cd member-service
./gradlew bootRun
```

#### 환경변수 설정
```bash
# application.properties의 ${VAR:default} 오버라이드
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/member_db?..."
export JWT_SECRET="your-secret-key"
export GOOGLE_CLIENT_ID="your-google-client-id"

./gradlew bootRun
```

### 6.3 프로덕션 배포 (Kubernetes)

#### 사전 요구사항
- AWS EKS 클러스터 설정
- kubectl 설치 및 클러스터 연결
- ArgoCD 설치

#### kubectl을 통한 수동 배포
```bash
# Namespace 생성
kubectl apply -f k8s/base/namespace.yaml

# ConfigMap 및 Secrets 배포
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml

# MySQL 배포
kubectl apply -f k8s/mysql/

# RabbitMQ 배포
kubectl apply -f k8s/rabbitmq/rabbitmq.yaml

# Eureka Server 배포 (먼저)
kubectl apply -f k8s/services/eureka-server.yaml

# Gateway 및 기타 서비스 배포
kubectl apply -f k8s/services/

# Ingress 배포
kubectl apply -f k8s/ingress.yaml

# HPA 배포
kubectl apply -f k8s/hpa/hpa.yaml
```

#### 배포 상태 확인
```bash
# Pod 상태 확인
kubectl get pods -n tori

# Service 확인
kubectl get svc -n tori

# HPA 상태 확인
kubectl get hpa -n tori

# 로그 확인
kubectl logs -f deployment/member-service -n tori
```

#### ArgoCD를 통한 GitOps 배포
```bash
# ArgoCD Root App 배포
kubectl apply -f k8s/argocd/root-app.yaml

# ArgoCD UI 접속
kubectl port-forward svc/argocd-server -n argocd 8080:443

# ArgoCD CLI로 sync
argocd app sync tori-root
argocd app get tori-root
```

**자동 배포 흐름**:
```
1. 코드 푸시 (main 브랜치)
2. GitHub Actions CI/CD 트리거
3. 9개 서비스 병렬 빌드 (matrix strategy)
4. Docker 이미지 빌드 및 ECR 푸시
5. ArgoCD가 Git 변경 감지 (auto-sync)
6. K8s 클러스터에 자동 배포
7. HPA에 의한 자동 스케일링
```

### 6.4 테스트 실행

#### 단위 테스트
```bash
# 모든 서비스 테스트
./gradlew test

# 특정 서비스 테스트
./gradlew :member-service:test

# 테스트 리포트 확인
open member-service/build/reports/tests/test/index.html
```

#### 통합 테스트
```bash
# Docker Compose 환경에서 통합 테스트
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

---

## 7. 인프라 구조 (Docker, K8s, 서버 구성 등)

### 7.1 로컬 개발 환경 (Docker Compose)

#### 네트워크 구조
```
tori-network (Bridge Network)
├── mysql-member:3306 (3306 외부 포트)
├── mysql-product:3306 (3307 외부 포트)
├── mysql-order:3306 (3308 외부 포트)
├── mysql-inventory:3306 (3309 외부 포트)
├── rabbitmq:5672, 15672
├── eureka-server:8761
├── gateway-service:8000
├── member-service:8004
├── product-service:8001
├── order-service:8002
├── inventory-service:8003
├── board-service:8005
├── frontend-service:8100
└── admin-service:8200
```

#### 데이터 지속성
```yaml
volumes:
  mysql-member-data:    # member_db 데이터
  mysql-product-data:   # product_db 데이터
  mysql-order-data:     # order_db 데이터
  mysql-inventory-data: # inventory_db 데이터
  rabbitmq-data:        # RabbitMQ 데이터
```

#### Health Check 설정
모든 서비스에 health check 정의:
- Interval: 30초
- Timeout: 10초
- Retries: 3회
- Test: `curl -f http://localhost:PORT/actuator/health`

### 7.2 프로덕션 환경 (AWS EKS + Kubernetes)

#### 클러스터 구조
```
AWS EKS Cluster
├── Namespace: tori
│   ├── StatefulSets (MySQL 4개)
│   ├── Deployments (9개 마이크로서비스 + RabbitMQ)
│   ├── Services (LoadBalancer, ClusterIP)
│   ├── ConfigMaps (app-config)
│   ├── Secrets (app-secrets)
│   ├── HPAs (7개 서비스)
│   └── Ingress (ALB)
└── Namespace: argocd
    └── ArgoCD Application
```

#### Kubernetes 리소스 구성

**StatefulSets (MySQL)**:
- `mysql-member`: PVC 20Gi, Service ClusterIP 3306
- `mysql-product`: PVC 20Gi, Service ClusterIP 3306
- `mysql-order`: PVC 20Gi, Service ClusterIP 3306
- `mysql-inventory`: PVC 20Gi, Service ClusterIP 3306

**Deployments (마이크로서비스)**:

| Service | Replicas (Min/Max) | CPU Request/Limit | Memory Request/Limit |
|---------|-------------------|-------------------|---------------------|
| eureka-server | 2 (고정) | 500m / 1000m | 512Mi / 1Gi |
| gateway-service | 2-10 | 500m / 1000m | 512Mi / 1Gi |
| member-service | 2-6 | 250m / 500m | 256Mi / 512Mi |
| product-service | 2-6 | 250m / 500m | 256Mi / 512Mi |
| order-service | 2-6 | 250m / 500m | 256Mi / 512Mi |
| inventory-service | 2-6 | 250m / 500m | 256Mi / 512Mi |
| board-service | 2-6 | 250m / 500m | 256Mi / 512Mi |
| admin-service | 2-6 | 250m / 500m | 256Mi / 512Mi |
| frontend-service | 2 (고정) | 250m / 500m | 256Mi / 512Mi |
| rabbitmq | 1 (고정) | 500m / 1000m | 512Mi / 1Gi |

**Services**:
- `eureka-server`: ClusterIP (8761)
- `gateway-service`: LoadBalancer (8000) - 외부 노출
- `member-service`: ClusterIP (8004)
- `product-service`: ClusterIP (8001)
- `order-service`: ClusterIP (8002)
- `inventory-service`: ClusterIP (8003)
- `board-service`: ClusterIP (8005)
- `frontend-service`: LoadBalancer (8100) - 외부 노출
- `admin-service`: ClusterIP (8200)
- `rabbitmq`: ClusterIP (5672, 15672)
- `mysql-*`: ClusterIP (3306)

**Ingress (AWS ALB)**:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tori-ingress
  namespace: tori
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  rules:
  - host: api.example.com  # 확인 필요
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway-service
            port:
              number: 8000
  - host: www.example.com  # 확인 필요
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend-service
            port:
              number: 8100
```

#### Horizontal Pod Autoscaler (HPA)

**스케일링 정책**:
- **Metrics**: CPU 70%, Memory 80%
- **Scale Up**: 즉시 (stabilizationWindow: 0s)
- **Scale Down**: 5분 안정화 (stabilizationWindow: 300s)

**서비스별 HPA**:
- gateway-service: 2-10 replicas
- member-service: 2-6 replicas
- product-service: 2-6 replicas
- order-service: 2-6 replicas
- inventory-service: 2-6 replicas
- board-service: 2-6 replicas
- admin-service: 2-6 replicas

**HPA 미적용**: eureka-server, frontend-service, rabbitmq (고정 replicas)

### 7.3 네트워크 아키텍처

#### 외부 → 내부 흐름
```
Internet
  ↓
AWS ALB (Ingress)
  ↓
├─→ api.example.com → gateway-service:8000 (LoadBalancer)
│     ↓
│     Eureka 조회 → 내부 서비스 (ClusterIP)
│
└─→ www.example.com → frontend-service:8100 (LoadBalancer)
      ↓
      gateway-service:8000 → 내부 서비스
```

#### 서비스 간 통신 (내부)
```
# 동기 통신 (REST)
gateway-service (8000)
  ├─→ member-service:8004 (ClusterIP)
  ├─→ product-service:8001 (ClusterIP)
  ├─→ order-service:8002 (ClusterIP)
  ├─→ inventory-service:8003 (ClusterIP)
  ├─→ board-service:8005 (ClusterIP)
  └─→ admin-service:8200 (ClusterIP)

# 비동기 통신 (RabbitMQ)
order-service → rabbitmq:5672 → inventory-service

# 서비스 디스커버리
모든 서비스 ↔ eureka-server:8761
```

#### 데이터베이스 접근
```
member-service ──┐
board-service ───┼─→ mysql-member:3306 (member_db)
admin-service ───┘

product-service ───→ mysql-product:3306 (product_db)
order-service ─────→ mysql-order:3306 (order_db)
inventory-service ─→ mysql-inventory:3306 (inventory_db)
```

### 7.4 CI/CD 파이프라인

#### GitHub Actions Workflow
```
Code Push (main 브랜치)
  ↓
Trigger GitHub Actions
  ↓
Matrix Strategy (9개 서비스 병렬)
  ├─→ Checkout Code
  ├─→ AWS Credentials 설정
  ├─→ ECR Login
  ├─→ ECR Repository 생성 (없으면)
  ├─→ Multi-stage Docker Build
  │   ├── Stage 1: Gradle 8 (빌드)
  │   └── Stage 2: Eclipse Temurin 17 JRE Alpine (런타임)
  ├─→ Docker Tag (latest)
  └─→ ECR Push
```

**Multi-stage Dockerfile 예시**:
```dockerfile
# Stage 1: Build
FROM gradle:8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### ArgoCD GitOps
```
ECR 이미지 푸시 완료
  ↓
ArgoCD Auto-Sync (polling or webhook)
  ↓
Git Repository 변경 감지
  ↓
K8s Manifest 적용
  ├─→ Rolling Update 전략
  ├─→ Health Check 대기
  └─→ 이전 Pod 종료
  ↓
HPA에 의한 자동 스케일링
```

### 7.5 모니터링 및 로깅 (확인 필요)

**현재 설정**:
- Spring Boot Actuator: `/actuator/health`, `/actuator/prometheus`
- Micrometer Prometheus: 메트릭 노출

**추가 필요 (확인 필요)**:
- Prometheus Server 설치 여부
- Grafana 대시보드 구성 여부
- ELK Stack (Elasticsearch, Logstash, Kibana) 또는 AWS CloudWatch 로깅
- Distributed Tracing (Zipkin, Jaeger) 설정 여부

### 7.6 보안 및 네트워크 정책 (확인 필요)

**현재 설정**:
- Kubernetes Secrets (Base64 인코딩)
- 환경변수를 통한 민감 정보 주입

**추가 권장 (확인 필요)**:
- AWS Secrets Manager 또는 Parameter Store 사용
- Network Policy를 통한 Pod 간 통신 제한
- TLS/SSL 인증서 (Let's Encrypt, AWS ACM)
- API Gateway에서 Rate Limiting 설정
- WAF (Web Application Firewall) 설정

---

## 8. 주요 아키텍처 결정 사항과 그 이유

### 8.1 마이크로서비스 아키텍처 (MSA) 채택

**결정**: 모놀리식 대신 9개의 독립적인 마이크로서비스로 분리

**확인된 근거**:
- **독립적인 배포**: 각 서비스가 별도의 Docker 이미지로 빌드되며, K8s Deployment로 독립 배포됨 (`.github/workflows/ci.yml` matrix strategy)
- **기술적 자율성**: 각 서비스가 독립적인 `build.gradle`, `application.properties` 보유
- **확장성**: HPA를 통해 서비스별로 다른 스케일링 정책 적용 (gateway: 2-10, 기타: 2-6)
- **장애 격리**: 한 서비스 장애가 전체 시스템에 영향을 주지 않음 (Eureka를 통한 동적 서비스 발견)

**트레이드오프**:
- 복잡도 증가: 9개 서비스 관리, 분산 트랜잭션 처리 필요
- 네트워크 오버헤드: 서비스 간 REST 호출 증가
- 운영 부담: 모니터링, 로깅, 디버깅이 어려움

### 8.2 Database per Service 패턴 (부분 적용)

**결정**: 4개의 독립적인 MySQL 데이터베이스 운영

**데이터베이스 분리 현황**:
```
product_db (3307)     → product-service 전용
order_db (3308)       → order-service 전용
inventory_db (3309)   → inventory-service 전용
member_db (3306)      → member, board, admin 서비스 공유
```

**확인된 근거**:
- **스키마 독립성**: 각 서비스가 자신의 스키마를 자율적으로 관리 (`spring.jpa.hibernate.ddl-auto=update`)
- **확장성**: 데이터베이스별로 독립적인 StatefulSet 및 PVC 할당 (K8s)
- **성능 격리**: 한 서비스의 DB 부하가 다른 서비스에 영향 없음

**예외 결정: member_db 공유**:
- member-service, board-service, admin-service가 `member_db:3306` 공유
- **추측되는 이유**:
  - 회원 정보에 대한 조인 쿼리 필요성 (board의 작성자 정보, admin의 회원 관리)
  - 데이터 일관성 보장 용이 (ACID 트랜잭션)
- **트레이드오프**: 서비스 간 데이터베이스 커플링 발생, 독립 배포 제약

### 8.3 API Gateway 패턴

**결정**: Spring Cloud Gateway를 단일 진입점으로 사용

**확인된 근거**:
- **중앙화된 라우팅**: 17개 라우트 규칙으로 모든 외부 요청 처리 (`gateway-service/application.properties`)
- **인증/인가**: JWT 검증 필터를 Gateway에서 일괄 처리 (추정)
- **CORS 관리**: Gateway에서 CORS 정책 중앙 설정
- **로드 밸런싱**: `lb://service-name` 프로토콜로 Eureka 기반 클라이언트 로드밸런싱

**기술 선택: Spring Cloud Gateway (WebFlux)**:
- 이유 (추정): 비동기 논블로킹 처리로 높은 동시성 지원
- 대안: Zuul (동기 Servlet 기반) 대신 선택

### 8.4 서비스 디스커버리 (Netflix Eureka)

**결정**: Netflix Eureka Server를 서비스 레지스트리로 사용

**확인된 근거**:
- **동적 서비스 발견**: 모든 서비스가 `eureka.client.service-url.defaultZone` 설정으로 등록
- **클라이언트측 로드밸런싱**: Gateway가 Eureka에서 서비스 인스턴스 목록 조회 후 분산
- **헬스체크**: Eureka가 주기적으로 서비스 상태 확인 (heartbeat)
- **K8s 환경에서도 유지**: K8s Service 대신 Eureka 사용 (이유 확인 필요)

**K8s DNS vs Eureka**:
- K8s 환경에서는 일반적으로 K8s Service DNS 사용이 권장됨
- 현재 프로젝트는 Eureka 유지 → **추측**: 로컬 Docker Compose와 K8s 양쪽 호환성 목적

### 8.5 Event-Driven Architecture (RabbitMQ)

**결정**: 주문 → 재고 통신에 RabbitMQ 메시지 큐 사용

**확인된 근거** (`order-service/OrderService.java:105`, `inventory-service/OrderMessageListener.java:15-18`):
```
주문 생성 → order-service가 "order.exchange"에 발행
         → RabbitMQ가 "inventory.order.queue"로 라우팅
         → inventory-service가 비동기 소비 및 재고 차감
```

**비동기 통신을 선택한 이유 (추정)**:
- **느슨한 결합**: order-service와 inventory-service 간 직접 의존성 제거
- **장애 허용**: inventory-service 다운 시에도 주문 생성 가능 (메시지 큐에 대기)
- **성능 향상**: 동기 호출 대기 시간 제거
- **트랜잭션 보상**: 재고 부족 시 주문 취소 이벤트 발행 가능 (Saga 패턴)

**REST vs 메시지 큐 선택 기준**:
- 동기 응답 필요: REST (Gateway를 통한 대부분의 통신)
- 비동기 처리 가능: RabbitMQ (주문 → 재고)

### 8.6 컨테이너화 및 오케스트레이션

**결정**: Docker + Kubernetes 기반 인프라

**Docker 사용 이유**:
- **일관된 실행 환경**: 로컬, 스테이징, 프로덕션 환경 동일화
- **Multi-stage Build**: 빌드 도구(Gradle 8)와 런타임(JRE 17 Alpine) 분리로 이미지 크기 최소화
- **확인된 이미지 크기 최적화**: Alpine 기반 JRE 사용 (경량화)

**Kubernetes 채택 이유**:
- **자동 스케일링**: HPA로 CPU/Memory 기반 Pod 자동 증감
- **자가 치유**: Pod 장애 시 자동 재시작 (Liveness/Readiness Probe)
- **선언적 배포**: YAML Manifest로 인프라를 코드로 관리
- **AWS EKS 사용**: 관리형 K8s로 운영 부담 감소

### 8.7 GitOps 배포 전략 (ArgoCD)

**결정**: ArgoCD를 통한 GitOps 기반 배포

**확인된 설정** (`k8s/argocd/root-app.yaml`):
```yaml
syncPolicy:
  automated:
    prune: true        # Git에서 삭제된 리소스 자동 제거
    selfHeal: true     # 클러스터 상태와 Git 상태 자동 동기화
```

**GitOps 채택 이유**:
- **단일 진실 공급원**: Git Repository가 인프라 상태의 유일한 source of truth
- **감사 추적**: 모든 변경사항이 Git 커밋 히스토리로 추적
- **롤백 용이**: Git revert로 간단히 이전 상태 복원
- **자동 동기화**: Git 푸시 → ArgoCD 자동 감지 → K8s 배포

**CI/CD 파이프라인**:
```
GitHub Push → Actions (빌드 + ECR 푸시) → ArgoCD (K8s 배포)
```

### 8.8 설정 외부화 (Externalized Configuration)

**결정**: 환경변수 + K8s ConfigMap/Secrets로 설정 관리

**확인된 패턴** (`application.properties`):
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/...}
jwt.secret=${JWT_SECRET:default-secret}
```

**설정 계층**:
1. **기본값**: `application.properties`의 `:default` 부분
2. **로컬 환경변수**: Docker Compose `environment` 섹션
3. **K8s ConfigMap**: 비민감 설정 (DB URL, Eureka URL)
4. **K8s Secrets**: 민감 정보 (DB 비밀번호, JWT Secret, OAuth2 Client Secret)

**이점**:
- 환경별 설정 분리 (로컬, 스테이징, 프로덕션)
- 코드 재빌드 없이 설정 변경 가능
- 민감 정보를 코드 저장소에서 분리

### 8.9 Auto-Scaling 전략

**결정**: HPA (Horizontal Pod Autoscaler) 사용

**확인된 정책** (`k8s/hpa/hpa.yaml`):
- **Metrics**: CPU 70%, Memory 80% 임계값
- **Scale Up**: 즉시 반응 (stabilizationWindow: 0s)
- **Scale Down**: 5분 안정화 후 축소 (stabilizationWindow: 300s)

**서비스별 차등 적용**:
- **Gateway (2-10)**: 높은 트래픽 대응
- **비즈니스 서비스 (2-6)**: 중간 수준 확장
- **Eureka, Frontend, RabbitMQ**: 고정 replicas (HPA 미적용)

**Scale Down 지연 이유**:
- 트래픽 급증 후 급감 시 불필요한 Pod 재생성 방지 (thrashing 방지)
- 5분간 부하 상태 관찰 후 안전하게 축소

### 8.10 데이터 지속성 전략

**결정**: StatefulSet + PersistentVolumeClaim (K8s)

**확인된 구성** (`k8s/mysql/*.yaml`):
- MySQL: StatefulSet으로 배포 (안정적인 네트워크 ID, 순차적 배포/종료)
- PVC: 각 MySQL 인스턴스당 20Gi 스토리지 할당
- RabbitMQ: Deployment + PVC (메시지 큐 데이터 보존)

**StatefulSet 선택 이유**:
- MySQL은 Stateful 애플리케이션 (데이터 지속성 필수)
- Pod 재시작 시 동일한 PVC 재연결 필요
- 네트워크 ID 고정 (`mysql-member-0`, `mysql-product-0` 등)

### 8.11 보안 아키텍처

**확인된 보안 메커니즘**:
- **JWT 인증**: HS512 알고리즘, Access Token (1시간), Refresh Token (7일)
- **OAuth2 Social Login**: Google, Naver 연동
- **K8s Secrets**: Base64 인코딩된 민감 정보 저장
- **HTTPS (확인 필요)**: Ingress에서 TLS 종료 여부 확인 필요

**보안 개선 필요 사항 (확인 필요)**:
- JWT Secret을 K8s Secret 대신 AWS Secrets Manager 사용
- API Rate Limiting (DDoS 방어)
- Network Policy (Pod 간 통신 제한)
- Container Image Scanning (취약점 검사)

### 8.12 모니터링 및 관측성 (Observability)

**현재 구현**:
- **Metrics**: Spring Boot Actuator + Micrometer Prometheus
- **Health Checks**: `/actuator/health` 엔드포인트
- **K8s Probes**: Liveness/Readiness Probe 설정

**확인 필요**:
- Prometheus Server 배포 여부
- Grafana 대시보드 구성
- Distributed Tracing (Zipkin, Jaeger)
- Centralized Logging (ELK Stack, AWS CloudWatch)

---

## 9. 확인 필요 사항 정리

다음 항목들은 실제 파일에서 명확히 확인되지 않았으므로, 추가 조사가 필요합니다:

1. **Ingress Host 설정**: `ingress.yaml`의 실제 도메인명 (api.example.com, www.example.com은 예시)
2. **Prometheus/Grafana**: 실제 설치 및 구성 여부
3. **Distributed Tracing**: Zipkin, Jaeger 등 분산 추적 시스템 사용 여부
4. **Centralized Logging**: ELK Stack, AWS CloudWatch 등 로깅 시스템 구성
5. **TLS/SSL 인증서**: Ingress에서 HTTPS 설정 여부
6. **API Rate Limiting**: Gateway에서 Rate Limiting 필터 구현 여부
7. **Network Policy**: K8s Network Policy 설정 여부
8. **AWS Secrets Manager**: K8s Secrets 대신 AWS Secrets Manager 사용 여부
9. **Circuit Breaker**: Resilience4j 등 서킷 브레이커 패턴 구현 여부
10. **Frontend-Backend 통신**: frontend-service가 Gateway를 통해 호출하는지, Eureka를 통해 직접 호출하는지
11. **RabbitMQ 추가 사용**: order → inventory 외에 다른 서비스 간 메시지 큐 사용 여부
12. **Spring Cloud Config Server**: 중앙화된 설정 서버 사용 여부 (현재 ConfigMap 사용 중)
13. **Database Migration Tool**: Flyway, Liquibase 등 DB 마이그레이션 도구 사용 여부 (현재 `ddl-auto=update` 사용)
14. **백업 전략**: MySQL 데이터베이스 백업 스케줄 및 복원 절차
15. **재해 복구 (DR)**: 멀티 리전 배포, 데이터 복제 전략

---

**문서 작성일**: 2026-04-14
**문서 버전**: 1.0
**작성 기준**: 실제 코드 및 설정 파일 분석 (추측 최소화)