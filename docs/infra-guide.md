# 인프라 가이드

## 목차
1. [인프라 개요](#1-인프라-개요)
2. [컨테이너 빌드 구조](#2-컨테이너-빌드-구조)
3. [로컬 개발 환경 (Docker Compose)](#3-로컬-개발-환경-docker-compose)
4. [프로덕션 배포 환경 (Kubernetes)](#4-프로덕션-배포-환경-kubernetes)
5. [CI/CD 파이프라인](#5-cicd-파이프라인)
6. [환경변수 관리](#6-환경변수-관리)
7. [모니터링 및 로깅](#7-모니터링-및-로깅)
8. [스케일링 설정](#8-스케일링-설정)
9. [운영 가이드](#9-운영-가이드)
10. [미구성 인프라](#10-미구성-인프라)

---

## 1. 인프라 개요

### 1.1 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                       Internet (사용자)                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│            AWS Application Load Balancer (ALB)               │
│                    (K8s Ingress)                             │
└──────────────┬─────────────────┬────────────────────────────┘
               │                 │
       / (Web UI)          /api (REST API)
               │                 │
               ▼                 ▼
    ┌──────────────────┐  ┌──────────────────┐
    │ Frontend Service │  │ Gateway Service  │
    │   (Thymeleaf)    │  │ (Spring Cloud)   │
    │     :8005        │  │     :8000        │
    └──────────────────┘  └────────┬─────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
         ┌────────────────┐ ┌───────────┐ ┌──────────────┐
         │ Member Service │ │  Product  │ │ Order Service│
         │     :8004      │ │  :8001    │ │    :8002     │
         └────────┬───────┘ └─────┬─────┘ └──────┬───────┘
                  │               │               │ (publish)
         ┌────────┴───────────────┴───────────────┤
         │                                         │
         ▼                                         ▼
    ┌──────────┐                            ┌──────────┐
    │ MySQL    │                            │ RabbitMQ │
    │ member_db│                            │  :5672   │
    │  :3306   │                            └────┬─────┘
    └──────────┘                                 │ (consume)
                                                 ▼
         ┌────────────────┐            ┌──────────────────┐
         │ Board Service  │            │Inventory Service │
         │     :8006      │            │      :8008       │
         └────────┬───────┘            └────────┬─────────┘
                  │                             │
         ┌────────┴──────────┐         ┌────────┴─────────┐
         │ MySQL             │         │ MySQL            │
         │ member_db (공유)  │         │ inventory_db     │
         └───────────────────┘         │     :3309        │
                                       └──────────────────┘
         ┌──────────────────┐
         │ Eureka Server    │ ◄────── (모든 서비스 등록)
         │     :8761        │
         └──────────────────┘
```

### 1.2 기술 스택 요약

| 계층 | 기술 | 버전 | 용도 |
|------|------|------|------|
| **컨테이너** | Docker | Latest | 애플리케이션 컨테이너화 |
| **오케스트레이션** | Kubernetes (AWS EKS) | Latest | 컨테이너 오케스트레이션 |
| **빌드 도구** | Gradle | 8 | Java 애플리케이션 빌드 |
| **런타임** | Eclipse Temurin JRE | 17 (Alpine) | Java 실행 환경 |
| **이미지 레지스트리** | AWS ECR | - | Docker 이미지 저장소 |
| **CI/CD** | GitHub Actions | - | 지속적 통합/배포 |
| **로드 밸런서** | AWS ALB | - | K8s Ingress Controller |
| **스토리지** | AWS EBS (gp2) | - | 영구 볼륨 |
| **네트워킹** | Bridge (로컬), VPC (K8s) | - | 컨테이너 네트워크 |

### 1.3 배포 환경

| 환경 | 플랫폼 | 목적 | 구성 파일 |
|------|--------|------|----------|
| **로컬 개발** | Docker Compose | 개발 및 테스트 | `docker-compose.yml` |
| **프로덕션** | AWS EKS (Kubernetes) | 운영 환경 | `k8s/**/*.yaml` |
| **스테이징** | 미구성 | - | - |

---

## 2. 컨테이너 빌드 구조

### 2.1 Dockerfile 패턴 (멀티스테이지 빌드)

모든 9개 마이크로서비스가 **동일한 멀티스테이지 빌드 패턴**을 사용합니다.

#### 표준 Dockerfile 구조

**예시**: `gateway-service/Dockerfile` (lines 1-12)

```dockerfile
# Build from root context: docker build -f gateway-service/Dockerfile .
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :gateway-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/gateway-service/build/libs/*.jar app.jar
EXPOSE 8000
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 멀티스테이지 빌드 분석

**Stage 1: Build Stage** (`gradle:8-jdk17`)
- **Base Image**: `gradle:8-jdk17` (빌드 도구 + JDK 포함)
- **작업**: Gradle을 사용한 애플리케이션 빌드
- **빌드 명령**: `gradle :서비스명:build -x test --no-daemon`
  - `-x test`: 테스트 스킵 (CI에서 별도 실행)
  - `--no-daemon`: Docker 환경에서 Gradle 데몬 비활성화
- **결과물**: `/app/서비스명/build/libs/*.jar`

**Stage 2: Runtime Stage** (`eclipse-temurin:17-jre-alpine`)
- **Base Image**: `eclipse-temurin:17-jre-alpine` (JRE만 포함, 경량화)
- **작업**: 빌드된 JAR 파일 복사 및 실행 환경 구성
- **최종 이미지 크기**: 약 200-300MB (JRE만 포함하여 경량화)
- **환경변수**: `SPRING_PROFILES_ACTIVE=docker`
- **엔트리포인트**: `java -jar app.jar`

#### 서비스별 Dockerfile 차이점

| 서비스 | Dockerfile 경로 | EXPOSE 포트 | 빌드 명령 |
|--------|----------------|------------|----------|
| eureka-server | `eureka-server/Dockerfile` | 8761 | `gradle :eureka-server:build -x test` |
| gateway-service | `gateway-service/Dockerfile` | 8000 | `gradle :gateway-service:build -x test` |
| member-service | `member-service/Dockerfile` | 8004 | `gradle :member-service:build -x test` |
| product-service | `product-service/Dockerfile` | 8001 | `gradle :product-service:build -x test` |
| order-service | `order-service/Dockerfile` | 8002 | `gradle :order-service:build -x test` |
| inventory-service | `inventory-service/Dockerfile` | 8008 | `gradle :inventory-service:build -x test` |
| board-service | `board-service/Dockerfile` | 8006 | `gradle :board-service:build -x test` |
| admin-service | `admin-service/Dockerfile` | 8007 | `gradle :admin-service:build -x test` |
| frontend-service | `frontend-service/Dockerfile` | 8005 | `gradle :frontend-service:build -x test` |

**패턴 일관성**: ✅ 모든 서비스가 동일한 패턴 사용

### 2.2 빌드 최적화

#### 레이어 캐싱 전략

**현재 구조**:
```dockerfile
COPY . .                                    # 전체 소스 복사
RUN gradle :gateway-service:build -x test   # 빌드 (의존성 + 컴파일)
```

**개선 가능 사항** (현재 미적용):
```dockerfile
# 의존성만 먼저 다운로드 (레이어 캐싱 최적화)
COPY build.gradle settings.gradle ./
COPY gateway-service/build.gradle ./gateway-service/
RUN gradle :gateway-service:dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY . .
RUN gradle :gateway-service:build -x test --no-daemon
```

### 2.3 이미지 빌드 방법

#### 로컬 빌드

**루트 디렉토리에서 실행**:
```bash
# 단일 서비스 빌드
docker build -f gateway-service/Dockerfile -t gateway-service:latest .

# 전체 서비스 빌드 (병렬)
services=("gateway-service" "eureka-server" "member-service" "product-service" "order-service" "inventory-service" "board-service" "admin-service" "frontend-service")
for service in "${services[@]}"; do
  docker build -f $service/Dockerfile -t $service:latest . &
done
wait
```

#### CI/CD 빌드

**GitHub Actions에서 자동 빌드** (`.github/workflows/ci.yml:46-51`):
```yaml
- name: Docker 빌드 & ECR 푸시
  env:
    ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
  run: |
    docker build -f ${{ matrix.service }}/Dockerfile -t $ECR_REGISTRY/${{ matrix.service }}:latest .
    docker push $ECR_REGISTRY/${{ matrix.service }}:latest
```

---

## 3. 로컬 개발 환경 (Docker Compose)

### 3.1 전체 구조

**파일**: `docker-compose.yml` (319 lines)

#### 서비스 구성 (14개)

| 카테고리 | 서비스 | 컨테이너명 | 포트 | 의존성 |
|---------|--------|-----------|------|--------|
| **데이터베이스** | mysql-member | mysql-member | 3306:3306 | - |
| | mysql-product | mysql-product | 3307:3306 | - |
| | mysql-order | mysql-order | 3308:3306 | - |
| | mysql-inventory | mysql-inventory | 3309:3306 | - |
| **메시지 큐** | rabbitmq | rabbitmq | 5672, 15672 | - |
| **인프라** | eureka-server | eureka-server | 8761:8761 | - |
| **API Gateway** | gateway-service | gateway-service | 8000:8000 | eureka-server |
| **프론트엔드** | frontend-service | frontend-service | 8005:8005 | eureka-server |
| **비즈니스** | member-service | member-service | 8004:8004 | eureka-server, mysql-member |
| | product-service | product-service | 8001:8001 | eureka-server, mysql-product, rabbitmq |
| | order-service | order-service | 8002:8002 | eureka-server, mysql-order, rabbitmq |
| | inventory-service | inventory-service | 8008:8008 | eureka-server, mysql-inventory, rabbitmq |
| | board-service | board-service | 8006:8006 | eureka-server, mysql-member |
| | admin-service | admin-service | 8007:8007 | eureka-server, mysql-member |

### 3.2 MySQL 데이터베이스 설정

#### 4개 독립 MySQL 인스턴스

**공통 설정**:
- Image: `mysql:8.0`
- Character Set: `utf8mb4`
- Collation: `utf8mb4_unicode_ci`
- Health Check: `mysqladmin ping` (10초 간격, 5초 타임아웃, 10회 재시도)

**데이터베이스별 상세 설정**:

**1. mysql-member** (`docker-compose.yml:9-28`)
```yaml
ports: "3306:3306"
environment:
  MYSQL_ROOT_PASSWORD: rootpass
  MYSQL_DATABASE: member_db
  MYSQL_USER: member_user
  MYSQL_PASSWORD: member_pass
volumes:
  - mysql-member-data:/var/lib/mysql
```
**사용 서비스**: member-service, board-service, admin-service

**2. mysql-product** (`docker-compose.yml:31-50`)
```yaml
ports: "3307:3306"  # 외부 포트 3307
MYSQL_DATABASE: product_db
MYSQL_USER: product_user
```
**사용 서비스**: product-service

**3. mysql-order** (`docker-compose.yml:53-72`)
```yaml
ports: "3308:3306"
MYSQL_DATABASE: order_db
MYSQL_USER: order_user
```
**사용 서비스**: order-service

**4. mysql-inventory** (`docker-compose.yml:75-94`)
```yaml
ports: "3309:3306"
MYSQL_DATABASE: inventory_db
MYSQL_USER: inventory_user
```
**사용 서비스**: inventory-service

### 3.3 RabbitMQ 설정

**RabbitMQ** (`docker-compose.yml:101-116`)
```yaml
image: rabbitmq:3-management-alpine
ports:
  - "5672:5672"    # AMQP 프로토콜
  - "15672:15672"  # Management UI
environment:
  RABBITMQ_DEFAULT_USER: guest
  RABBITMQ_DEFAULT_PASS: guest
healthcheck:
  test: rabbitmq-diagnostics -q ping
  interval: 10s
  timeout: 5s
  retries: 5
```

**Management UI 접속**: http://localhost:15672 (guest/guest)

### 3.4 네트워크 및 볼륨

#### 네트워크

**Bridge Network** (`docker-compose.yml:312-313`):
```yaml
networks:
  tori-network:
    driver: bridge
```

모든 컨테이너가 `tori-network`에 연결되어 내부 DNS로 통신:
- `mysql-member:3306`
- `rabbitmq:5672`
- `eureka-server:8761`

#### 영구 볼륨

**볼륨 정의** (`docker-compose.yml:315-319`):
```yaml
volumes:
  mysql-member-data:
  mysql-product-data:
  mysql-order-data:
  mysql-inventory-data:
```

**데이터 영속성**: Docker 볼륨에 MySQL 데이터 저장 → 컨테이너 재시작 시 데이터 유지

### 3.5 서비스 시작 순서 및 Health Check

#### 의존성 체인

```
1. MySQL (4개) + RabbitMQ
   ↓ (health check 대기)
2. eureka-server
   ↓ (health check 대기)
3. gateway-service, frontend-service
4. member-service, product-service, order-service, inventory-service, board-service, admin-service
```

#### Health Check 설정

**eureka-server** (`docker-compose.yml:126-130`):
```yaml
healthcheck:
  test: ["CMD", "wget", "-q", "--spider", "http://localhost:8761/actuator/health"]
  interval: 10s
  timeout: 5s
  retries: 10
```

**의존성 예시** (`docker-compose.yml:189-194`):
```yaml
depends_on:
  eureka-server:
    condition: service_healthy  # eureka-server가 healthy 상태일 때만 시작
  mysql-member:
    condition: service_healthy
```

### 3.6 환경변수 주입

#### ConfigMap 방식 환경변수

**gateway-service** (`docker-compose.yml:142-145`):
```yaml
environment:
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
  INQUIRY_SERVICE_URL: http://admin-service:8007
  FRONTEND_SERVICE_URL: http://frontend-service:8005
```

**member-service** (`docker-compose.yml:182-188`):
```yaml
env_file:
  - .env  # 민감 정보 (JWT_SECRET, OAuth2 등)
environment:
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql-member:3306/member_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
  SPRING_DATASOURCE_USERNAME: member_user
  SPRING_DATASOURCE_PASSWORD: member_pass
```

**`.env` 파일 예시** (프로젝트 루트에 생성 필요):
```bash
# JWT
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters-long
JWT_EXPIRATION=3600000

# OAuth2 - Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# OAuth2 - Naver
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# Email (Gmail SMTP)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 3.7 로컬 개발 환경 실행 가이드

#### 사전 요구사항

1. Docker Desktop 설치
2. `.env` 파일 생성 (민감 정보 설정)
3. 포트 충돌 확인 (3306-3309, 5672, 8000-8008, 8761, 15672)

#### 실행 명령어

**전체 환경 시작**:
```bash
# 백그라운드 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f gateway-service
```

**개별 서비스 재시작**:
```bash
docker-compose restart member-service
```

**전체 환경 중지 및 정리**:
```bash
# 컨테이너 중지
docker-compose down

# 볼륨까지 삭제 (데이터베이스 초기화)
docker-compose down -v
```

#### 접속 URL

| 서비스 | URL | 설명 |
|--------|-----|------|
| Frontend | http://localhost:8005 | 웹 UI |
| API Gateway | http://localhost:8000 | REST API |
| Eureka Dashboard | http://localhost:8761 | 서비스 레지스트리 |
| RabbitMQ Management | http://localhost:15672 | 메시지 큐 관리 (guest/guest) |
| Member Service | http://localhost:8004 | 직접 접근 (디버깅용) |
| Product Service | http://localhost:8001 | 직접 접근 |

#### 트러블슈팅

**1. 포트 충돌**
```bash
# 포트 사용 확인 (Windows)
netstat -ano | findstr :3306

# 프로세스 종료
taskkill /PID <프로세스ID> /F
```

**2. MySQL 연결 실패**
```bash
# MySQL 컨테이너 로그 확인
docker-compose logs mysql-member

# Health check 상태 확인
docker-compose ps
```

**3. Eureka 등록 실패**
```bash
# Eureka 대시보드에서 서비스 등록 확인
# http://localhost:8761

# 서비스 로그에서 eureka 연결 확인
docker-compose logs member-service | grep eureka
```

---

## 4. 프로덕션 배포 환경 (Kubernetes)

### 4.1 클러스터 구성

**플랫폼**: AWS EKS (Elastic Kubernetes Service)
**네임스페이스**: `tori-app` (`k8s/base/namespace.yaml:4`)
**리전**: `ap-northeast-2` (서울)

### 4.2 Kubernetes 리소스 구조

```
k8s/
├── base/
│   ├── namespace.yaml          # Namespace: tori-app
│   ├── configmap.yaml          # 비민감 환경변수
│   └── secrets.yaml            # 민감 정보 (DB, JWT, OAuth2)
├── services/
│   ├── eureka-server.yaml      # Deployment + Service
│   ├── gateway-service.yaml
│   ├── member-service.yaml
│   ├── product-service.yaml
│   ├── order-service.yaml
│   ├── inventory-service.yaml
│   ├── board-service.yaml
│   ├── admin-service.yaml
│   └── frontend-service.yaml
├── mysql/
│   ├── mysql-member.yaml       # StatefulSet + PVC + Service
│   ├── mysql-product.yaml
│   ├── mysql-order.yaml
│   └── mysql-inventory.yaml
├── rabbitmq/
│   └── rabbitmq.yaml           # StatefulSet + PVC + Service
├── hpa/
│   └── hpa.yaml                # HorizontalPodAutoscaler (7개 서비스)
├── ingress.yaml                # ALB Ingress Controller
└── cluster-config.yaml         # 클러스터 설정 (확인 필요)
```

### 4.3 ConfigMap 및 Secrets

#### ConfigMap (비민감 환경변수)

**파일**: `k8s/base/configmap.yaml` (22 lines)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: tori-app
data:
  EUREKA_SERVER_URL: "http://eureka-server:8761/eureka/"
  EUREKA_INSTANCE_PREFER_IP_ADDRESS: "true"
  RABBITMQ_HOST: "rabbitmq"
  RABBITMQ_PORT: "5672"
  # MySQL hosts
  MYSQL_MEMBER_HOST: "mysql-member"
  MYSQL_PRODUCT_HOST: "mysql-product"
  MYSQL_ORDER_HOST: "mysql-order"
  MYSQL_INVENTORY_HOST: "mysql-inventory"
  # Service URLs (내부 통신)
  SERVICE_MEMBER_URL: "http://member-service:8004"
  SERVICE_BOARD_URL: "http://board-service:8006"
  SERVICE_ADMIN_URL: "http://admin-service:8007"
  SERVICE_PRODUCT_URL: "http://product-service:8001"
  SERVICE_ORDER_URL: "http://order-service:8002"
  SERVICE_INVENTORY_URL: "http://inventory-service:8008"
```

#### Secrets (민감 정보)

**파일**: `k8s/base/secrets.yaml` (39 lines)

**db-secrets**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-secrets
  namespace: tori-app
type: Opaque
stringData:
  # MySQL 4개 DB 인증 정보
  MYSQL_MEMBER_USER: "member_user"
  MYSQL_MEMBER_PASSWORD: "your-password-here"
  MYSQL_MEMBER_ROOT_PASSWORD: "your-root-password-here"
  # ... (product, order, inventory 동일 패턴)

  # RabbitMQ
  RABBITMQ_USER: "guest"
  RABBITMQ_PASSWORD: "your-password-here"

  # JWT
  JWT_SECRET: "your-256-bit-secret-key-here-must-be-at-least-32-characters-long"
```

**mail-secrets**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mail-secrets
  namespace: tori-app
type: Opaque
stringData:
  MAIL_USERNAME: "your-email@gmail.com"
  MAIL_PASSWORD: "your-app-password-here"
```

**⚠️ 프로덕션 배포 시**:
- `your-password-here`, `your-email@gmail.com` 등을 실제 값으로 변경
- Secrets는 Git에 커밋하지 않고, AWS Secrets Manager 또는 Sealed Secrets 사용 권장

### 4.4 Deployment 예시 (gateway-service)

**파일**: `k8s/services/gateway-service.yaml` (69 lines)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-service
  namespace: tori-app
spec:
  replicas: 1  # HPA가 자동 조정
  selector:
    matchLabels:
      app: gateway-service
  template:
    metadata:
      labels:
        app: gateway-service
    spec:
      containers:
        - name: gateway-service
          image: 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/gateway-service:latest
          ports:
            - containerPort: 8000
          env:
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: EUREKA_SERVER_URL
            - name: INQUIRY_SERVICE_URL
              value: "http://admin-service:8007"
          resources:
            requests:
              memory: "384Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            tcpSocket:
              port: 8000
            initialDelaySeconds: 120
            periodSeconds: 30
            failureThreshold: 5
          readinessProbe:
            tcpSocket:
              port: 8000
            initialDelaySeconds: 60
            periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
  namespace: tori-app
spec:
  selector:
    app: gateway-service
  ports:
    - port: 8000
      targetPort: 8000
  type: ClusterIP
```

**주요 설정**:
- **Image**: AWS ECR에서 자동 pull (`490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/gateway-service:latest`)
- **Resources**: CPU 250m-500m, Memory 384Mi-512Mi
- **Probes**: Liveness (120초 후 시작, 30초 간격), Readiness (60초 후 시작, 10초 간격)
- **Service Type**: ClusterIP (내부 통신만)

### 4.5 StatefulSet 예시 (mysql-member)

**파일**: `k8s/mysql/mysql-member.yaml` (103 lines)

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-member-pvc
  namespace: tori-app
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: gp2  # AWS EBS
  resources:
    requests:
      storage: 10Gi
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql-member
  namespace: tori-app
spec:
  serviceName: mysql-member
  replicas: 1
  selector:
    matchLabels:
      app: mysql-member
  template:
    metadata:
      labels:
        app: mysql-member
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          ports:
            - containerPort: 3306
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: MYSQL_MEMBER_ROOT_PASSWORD
            - name: MYSQL_DATABASE
              value: "member_db"
            - name: MYSQL_USER
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: MYSQL_MEMBER_USER
            - name: MYSQL_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: MYSQL_MEMBER_PASSWORD
          args:
            - --character-set-server=utf8mb4
            - --collation-server=utf8mb4_unicode_ci
          volumeMounts:
            - name: mysql-data
              mountPath: /var/lib/mysql
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            exec:
              command: ["mysqladmin", "ping", "-h", "localhost"]
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            exec:
              command: ["mysqladmin", "ping", "-h", "localhost"]
            initialDelaySeconds: 10
            periodSeconds: 5
      volumes:
        - name: mysql-data
          persistentVolumeClaim:
            claimName: mysql-member-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-member
  namespace: tori-app
spec:
  selector:
    app: mysql-member
  ports:
    - port: 3306
      targetPort: 3306
  clusterIP: None  # Headless Service
```

**주요 설정**:
- **PVC**: 10Gi EBS gp2 볼륨
- **StatefulSet**: 안정적인 네트워크 ID 및 순차 배포
- **Service**: Headless (clusterIP: None) - StatefulSet용

### 4.6 RabbitMQ StatefulSet

**파일**: `k8s/rabbitmq/rabbitmq.yaml` (89 lines)

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rabbitmq
  namespace: tori-app
spec:
  serviceName: rabbitmq
  replicas: 1
  selector:
    matchLabels:
      app: rabbitmq
  template:
    spec:
      containers:
        - name: rabbitmq
          image: rabbitmq:3-management-alpine
          ports:
            - containerPort: 5672
              name: amqp
            - containerPort: 15672
              name: management
          env:
            - name: RABBITMQ_DEFAULT_USER
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: RABBITMQ_USER
            - name: RABBITMQ_DEFAULT_PASS
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: RABBITMQ_PASSWORD
          resources:
            requests:
              memory: "512Mi"
              cpu: "200m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          volumeMounts:
            - name: rabbitmq-data
              mountPath: /var/lib/rabbitmq
  volumeClaimTemplates:
    - metadata:
        name: rabbitmq-data
      spec:
        accessModes: ["ReadWriteOnce"]
        storageClassName: gp2
        resources:
          requests:
            storage: 1Gi
---
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq
  namespace: tori-app
spec:
  selector:
    app: rabbitmq
  ports:
    - name: amqp
      port: 5672
      targetPort: 5672
    - name: management
      port: 15672
      targetPort: 15672
```

**주요 설정**:
- **volumeClaimTemplates**: StatefulSet이 자동으로 PVC 생성
- **Storage**: 1Gi (메시지 큐 데이터)
- **Ports**: 5672 (AMQP), 15672 (Management UI)

### 4.7 Ingress (ALB)

**파일**: `k8s/ingress.yaml` (43 lines)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tori-app-ingress
  namespace: tori-app
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '5'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '2'
spec:
  ingressClassName: alb
  rules:
    - http:
        paths:
          # Frontend - 메인 진입점
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service
                port:
                  number: 8005
          # API Gateway
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 8000
          # Eureka Dashboard (관리용)
          - path: /eureka
            pathType: Prefix
            backend:
              service:
                name: eureka-server
                port:
                  number: 8761
```

**라우팅 규칙**:
- `/` → frontend-service:8005 (웹 UI)
- `/api/*` → gateway-service:8000 (REST API)
- `/eureka/*` → eureka-server:8761 (서비스 레지스트리 대시보드)

**ALB Health Check**:
- Path: `/actuator/health`
- Interval: 15초
- Timeout: 5초
- Threshold: 2회 성공/실패

### 4.8 K8s 배포 가이드

#### 사전 요구사항

1. **AWS EKS 클러스터 생성**
2. **kubectl 설치 및 클러스터 연결**
   ```bash
   aws eks update-kubeconfig --region ap-northeast-2 --name tori-cluster
   ```
3. **AWS Load Balancer Controller 설치**
   ```bash
   kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=master"
   helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
     -n kube-system \
     --set clusterName=tori-cluster
   ```

#### 배포 순서

**1. Namespace 및 기본 설정**
```bash
kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml  # 민감 정보 수정 후
```

**2. 인프라 (MySQL, RabbitMQ)**
```bash
kubectl apply -f k8s/mysql/
kubectl apply -f k8s/rabbitmq/

# StatefulSet이 Running 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod -l app=mysql-member -n tori-app --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n tori-app --timeout=300s
```

**3. Eureka Server (서비스 디스커버리)**
```bash
kubectl apply -f k8s/services/eureka-server.yaml

# Eureka가 Ready 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod -l app=eureka-server -n tori-app --timeout=300s
```

**4. 마이크로서비스 및 Gateway**
```bash
kubectl apply -f k8s/services/

# 모든 Pod가 Running 상태 확인
kubectl get pods -n tori-app -w
```

**5. HPA 및 Ingress**
```bash
kubectl apply -f k8s/hpa/hpa.yaml
kubectl apply -f k8s/ingress.yaml

# ALB 생성 확인 (2-3분 소요)
kubectl get ingress -n tori-app
```

#### 배포 확인

```bash
# 모든 리소스 확인
kubectl get all -n tori-app

# Pod 상태 확인
kubectl get pods -n tori-app -o wide

# Service 확인
kubectl get svc -n tori-app

# Ingress 확인 (ALB URL 획득)
kubectl get ingress -n tori-app
```

#### 로그 및 디버깅

```bash
# Pod 로그 확인
kubectl logs -f deployment/gateway-service -n tori-app

# Pod 내부 접속
kubectl exec -it <pod-name> -n tori-app -- /bin/sh

# ConfigMap 확인
kubectl get configmap app-config -n tori-app -o yaml

# Secret 확인 (Base64 디코딩)
kubectl get secret db-secrets -n tori-app -o jsonpath='{.data.JWT_SECRET}' | base64 -d
```

#### 롤백

```bash
# Deployment 롤백
kubectl rollout undo deployment/gateway-service -n tori-app

# 롤아웃 히스토리 확인
kubectl rollout history deployment/gateway-service -n tori-app
```

---

## 5. CI/CD 파이프라인

### 5.1 GitHub Actions Workflow

**파일**: `.github/workflows/ci.yml` (51 lines)

#### Trigger 조건

```yaml
on:
  push:
    branches: [ main ]
```

- **main 브랜치에 push** 시 자동 실행
- Pull Request merge 시에도 실행

#### 전역 환경변수

```yaml
env:
  AWS_REGION: ap-northeast-2  # 서울 리전
```

### 5.2 Matrix Build 전략

**병렬 빌드**: 9개 서비스를 동시에 빌드 (`.github/workflows/ci.yml:14-25`)

```yaml
strategy:
  matrix:
    service:
      - admin-service
      - board-service
      - eureka-server
      - frontend-service
      - gateway-service
      - inventory-service
      - member-service
      - order-service
      - product-service
```

**장점**:
- 빌드 시간 단축 (9개 서비스 병렬 처리)
- 한 서비스 실패해도 다른 서비스 빌드 계속 진행

### 5.3 CI/CD 단계

#### Step 1: 코드 체크아웃

```yaml
- uses: actions/checkout@v3
```

#### Step 2: AWS 인증

```yaml
- name: AWS 인증
  uses: aws-actions/configure-aws-credentials@v2
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    aws-region: ${{ env.AWS_REGION }}
```

**필요한 GitHub Secrets**:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

#### Step 3: ECR 로그인

```yaml
- name: ECR 로그인
  id: login-ecr
  uses: aws-actions/amazon-ecr-login@v1
```

**출력**:
- `steps.login-ecr.outputs.registry`: ECR 레지스트리 URL

#### Step 4: ECR 레포지토리 자동 생성

```yaml
- name: ECR 레포지토리 자동 생성
  run: |
    aws ecr describe-repositories --repository-names ${{ matrix.service }} || \
    aws ecr create-repository --repository-name ${{ matrix.service }}
```

**동작**:
- ECR 레포지토리가 없으면 자동 생성
- 이미 존재하면 skip

#### Step 5: Docker 빌드 및 ECR 푸시

```yaml
- name: Docker 빌드 & ECR 푸시 - ${{ matrix.service }}
  env:
    ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
  run: |
    docker build -f ${{ matrix.service }}/Dockerfile -t $ECR_REGISTRY/${{ matrix.service }}:latest .
    docker push $ECR_REGISTRY/${{ matrix.service }}:latest
```

**이미지 태그**: `490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/{service}:latest`

### 5.4 배포 자동화 (미구성)

**현재 상태**:
- ✅ CI (빌드 및 ECR 푸시): GitHub Actions
- ❌ CD (K8s 배포): **미구성**

**배포 방법 (현재)**:
1. GitHub Actions가 ECR에 이미지 푸시
2. 수동으로 `kubectl set image` 또는 `kubectl rollout restart` 실행

**권장 개선**:
- **ArgoCD** 연동 (GitOps)
- **Flux CD** 사용
- **GitHub Actions에서 kubectl 직접 실행**

### 5.5 CD 구성 예시 (ArgoCD)

**ArgoCD Application 매니페스트** (현재 미구성):
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: tori-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/<your-org>/teamprojectv1
    targetRevision: main
    path: k8s
  destination:
    server: https://kubernetes.default.svc
    namespace: tori-app
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

---

## 6. 환경변수 관리

### 6.1 환경변수 계층 구조

```
1. application.properties (기본값)
   ${VAR:default-value}
   ↓
2. Docker Compose environment / .env
   SPRING_DATASOURCE_URL=jdbc:mysql://...
   ↓
3. Kubernetes ConfigMap (비민감)
   EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
   ↓
4. Kubernetes Secrets (민감)
   MYSQL_MEMBER_PASSWORD=***
```

### 6.2 환경변수 전체 목록

#### 공통 환경변수

| 변수명 | 설정 위치 | 예시 값 | 설명 |
|--------|----------|---------|------|
| `SPRING_PROFILES_ACTIVE` | Dockerfile | `docker` | 활성 프로파일 |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | ConfigMap | `http://eureka-server:8761/eureka/` | Eureka 서버 URL |
| `EUREKA_INSTANCE_PREFER_IP_ADDRESS` | ConfigMap | `true` | IP 기반 등록 |

#### 데이터베이스 환경변수

**member-service, board-service, admin-service**:
| 변수명 | 설정 위치 | 예시 값 |
|--------|----------|---------|
| `SPRING_DATASOURCE_URL` | ConfigMap/Env | `jdbc:mysql://mysql-member:3306/member_db?useSSL=false&serverTimezone=Asia/Seoul` |
| `SPRING_DATASOURCE_USERNAME` | Secrets | `member_user` |
| `SPRING_DATASOURCE_PASSWORD` | Secrets | `***` |

**product-service**:
| 변수명 | 설정 위치 | 예시 값 |
|--------|----------|---------|
| `SPRING_DATASOURCE_URL` | ConfigMap/Env | `jdbc:mysql://mysql-product:3306/product_db?...` |
| `SPRING_DATASOURCE_USERNAME` | Secrets | `product_user` |
| `SPRING_DATASOURCE_PASSWORD` | Secrets | `***` |

**(order-service, inventory-service도 동일 패턴)**

#### RabbitMQ 환경변수

**product-service, order-service, inventory-service**:
| 변수명 | 설정 위치 | 예시 값 |
|--------|----------|---------|
| `SPRING_RABBITMQ_HOST` | ConfigMap | `rabbitmq` |
| `SPRING_RABBITMQ_PORT` | ConfigMap | `5672` |
| `RABBITMQ_DEFAULT_USER` | Secrets | `guest` |
| `RABBITMQ_DEFAULT_PASS` | Secrets | `***` |

#### JWT 및 인증 환경변수

**member-service**:
| 변수명 | 설정 위치 | 예시 값 | 설명 |
|--------|----------|---------|------|
| `JWT_SECRET` | Secrets | `your-256-bit-secret-...` | JWT 서명 키 (최소 32자) |
| `JWT_EXPIRATION` | .env | `3600000` | Access Token 만료 시간 (1시간) |
| `GOOGLE_CLIENT_ID` | Secrets | `***` | OAuth2 Google |
| `GOOGLE_CLIENT_SECRET` | Secrets | `***` | OAuth2 Google |
| `NAVER_CLIENT_ID` | Secrets | `***` | OAuth2 Naver |
| `NAVER_CLIENT_SECRET` | Secrets | `***` | OAuth2 Naver |

#### 이메일 환경변수

**member-service**:
| 변수명 | 설정 위치 | 예시 값 | 설명 |
|--------|----------|---------|------|
| `MAIL_USERNAME` | Secrets | `your-email@gmail.com` | Gmail SMTP 사용자 |
| `MAIL_PASSWORD` | Secrets | `***` | Gmail 앱 비밀번호 |

#### 서비스 간 통신 URL

**frontend-service**:
| 변수명 | 설정 위치 | 예시 값 |
|--------|----------|---------|
| `SERVICE_MEMBER_URL` | ConfigMap | `http://member-service:8004` |
| `SERVICE_BOARD_URL` | ConfigMap | `http://board-service:8006` |
| `SERVICE_ADMIN_URL` | ConfigMap | `http://admin-service:8007` |
| `SERVICE_PRODUCT_URL` | ConfigMap | `http://product-service:8001` |
| `SERVICE_ORDER_URL` | ConfigMap | `http://order-service:8002` |
| `SERVICE_INVENTORY_URL` | ConfigMap | `http://inventory-service:8008` |

### 6.3 환경변수 추가 방법

#### 로컬 개발 (Docker Compose)

**1. 비민감 정보**: `docker-compose.yml`에 직접 추가
```yaml
environment:
  NEW_CONFIG: "value"
```

**2. 민감 정보**: `.env` 파일에 추가
```bash
# .env
NEW_SECRET=your-secret-value
```

그리고 `docker-compose.yml`에서 참조:
```yaml
env_file:
  - .env
```

#### 프로덕션 (Kubernetes)

**1. 비민감 정보**: `k8s/base/configmap.yaml` 수정
```yaml
data:
  NEW_CONFIG: "value"
```

**2. 민감 정보**: `k8s/base/secrets.yaml` 수정
```yaml
stringData:
  NEW_SECRET: "your-secret-value"
```

**3. Deployment에서 참조**:
```yaml
env:
  - name: NEW_CONFIG
    valueFrom:
      configMapKeyRef:
        name: app-config
        key: NEW_CONFIG
  - name: NEW_SECRET
    valueFrom:
      secretKeyRef:
        name: db-secrets
        key: NEW_SECRET
```

**4. 적용**:
```bash
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml
kubectl rollout restart deployment/gateway-service -n tori-app
```

---

## 7. 모니터링 및 로깅

### 7.1 현재 구성

#### ✅ Spring Boot Actuator

**모든 서비스에 활성화**:
- Endpoint: `/actuator/health`, `/actuator/info`, `/actuator/prometheus`
- Liveness Probe: K8s에서 `/actuator/health` 사용
- Metrics: Prometheus 포맷으로 노출

**Health Check 설정** (`k8s/services/gateway-service.yaml:43-56`):
```yaml
livenessProbe:
  tcpSocket:
    port: 8000
  initialDelaySeconds: 120
  periodSeconds: 30
  failureThreshold: 5
readinessProbe:
  tcpSocket:
    port: 8000
  initialDelaySeconds: 60
  periodSeconds: 10
```

**Ingress Health Check** (`k8s/ingress.yaml:10`):
```yaml
alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
```

#### ✅ Kubernetes Probes

**Liveness Probe**: Pod가 정상 작동 중인지 확인
- 실패 시: Pod 재시작

**Readiness Probe**: Pod가 트래픽을 받을 준비가 되었는지 확인
- 실패 시: Service에서 제외 (트래픽 차단)

### 7.2 미구성 인프라

#### ❌ Prometheus Server

**현재 상태**: Metrics는 노출되지만 수집 서버 없음

**구성 권장**:
```yaml
# prometheus.yaml (미구성)
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    scrape_configs:
      - job_name: 'spring-boot'
        kubernetes_sd_configs:
          - role: pod
            namespaces:
              names:
                - tori-app
        relabel_configs:
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
            action: keep
            regex: true
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
            action: replace
            target_label: __metrics_path__
            regex: (.+)
```

#### ❌ Grafana Dashboard

**현재 상태**: 시각화 도구 없음

**구성 권장**:
- Grafana 설치 (Helm Chart)
- Spring Boot Dashboard 임포트 (ID: 4701, 11378)
- Custom Dashboard 구성 (JVM, HTTP, DB 메트릭)

#### ❌ Centralized Logging

**현재 상태**: 각 Pod 로그는 `kubectl logs`로만 확인 가능

**구성 권장 (ELK Stack)**:
- **Elasticsearch**: 로그 저장소
- **Logstash/Fluentd**: 로그 수집
- **Kibana**: 로그 검색 및 시각화

**대안 (AWS CloudWatch)**:
- Fluent Bit DaemonSet 배포
- CloudWatch Logs로 전송
- CloudWatch Insights로 쿼리

#### ❌ Distributed Tracing

**현재 상태**: 서비스 간 요청 추적 불가

**구성 권장 (Jaeger/Zipkin)**:
- Spring Cloud Sleuth 추가 (`build.gradle`)
- Jaeger Agent 배포
- Trace ID를 통한 요청 추적

### 7.3 로그 확인 방법 (현재)

#### Docker Compose

```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스
docker-compose logs -f gateway-service

# 최근 100줄
docker-compose logs --tail=100 gateway-service
```

#### Kubernetes

```bash
# Pod 로그 확인
kubectl logs -f deployment/gateway-service -n tori-app

# 이전 컨테이너 로그 (재시작된 경우)
kubectl logs --previous <pod-name> -n tori-app

# 여러 Pod 로그 동시 확인 (stern 사용)
stern gateway-service -n tori-app

# 로그 파일로 저장
kubectl logs deployment/gateway-service -n tori-app > gateway.log
```

---

## 8. 스케일링 설정

### 8.1 Horizontal Pod Autoscaler (HPA)

**파일**: `k8s/hpa/hpa.yaml` (239 lines)

#### HPA 적용 서비스 (7개)

| 서비스 | Min Replicas | Max Replicas | CPU 임계값 | Memory 임계값 |
|--------|-------------|--------------|-----------|--------------|
| frontend-service | 2 | 10 | 70% | 80% |
| gateway-service | 2 | 10 | 70% | 80% |
| member-service | 2 | 8 | 70% | 80% |
| product-service | 2 | 8 | 70% | 80% |
| order-service | 2 | 10 | 70% | 80% |
| inventory-service | 2 | 8 | 70% | 80% |
| board-service | 2 | 6 | 70% | 80% |
| admin-service | 2 | 6 | 70% | 80% |

#### HPA 미적용 서비스

- **eureka-server**: 1 replica 고정 (서비스 디스커버리는 단일 인스턴스)
- **MySQL (4개)**: StatefulSet, 1 replica 고정
- **RabbitMQ**: StatefulSet, 1 replica 고정

### 8.2 HPA 상세 설정

#### gateway-service HPA 예시

**파일**: `k8s/hpa/hpa.yaml:46-83`

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: gateway-service-hpa
  namespace: tori-app
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
    scaleDown:
      stabilizationWindowSeconds: 300  # 5분간 안정화 후 축소
      policies:
        - type: Percent
          value: 10
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0  # 즉시 스케일업
      policies:
        - type: Percent
          value: 100
          periodSeconds: 15
```

#### Scaling Behavior 상세

**Scale Up (확장)**:
- **stabilizationWindow**: 0초 (즉시 반응)
- **정책**:
  - 15초마다 최대 100% 증가 (현재 2개 → 4개)
  - 또는 15초마다 최대 4개 Pod 추가

**Scale Down (축소)**:
- **stabilizationWindow**: 300초 (5분 안정화)
- **정책**:
  - 60초마다 최대 10% 감소
- **이유**: 트래픽 급증 후 급감 시 불필요한 Pod 재생성 방지

### 8.3 Resource Requests & Limits

#### 서비스별 리소스 설정

**gateway-service** (`k8s/services/gateway-service.yaml:36-42`):
```yaml
resources:
  requests:
    memory: "384Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

**MySQL** (`k8s/mysql/mysql-member.yaml:61-67`):
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

**RabbitMQ** (`k8s/rabbitmq/rabbitmq.yaml:36-42`):
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "200m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

#### 리소스 요약 표

| 서비스 | CPU Request | CPU Limit | Memory Request | Memory Limit |
|--------|------------|-----------|----------------|--------------|
| gateway-service | 250m | 500m | 384Mi | 512Mi |
| member-service | 250m | 500m | 384Mi | 512Mi |
| product-service | 250m | 500m | 384Mi | 512Mi |
| order-service | 250m | 500m | 384Mi | 512Mi |
| inventory-service | 250m | 500m | 384Mi | 512Mi |
| board-service | 250m | 500m | 384Mi | 512Mi |
| admin-service | 250m | 500m | 384Mi | 512Mi |
| frontend-service | 250m | 500m | 384Mi | 512Mi |
| eureka-server | 250m | 500m | 384Mi | 512Mi |
| MySQL (각) | 250m | 500m | 256Mi | 512Mi |
| RabbitMQ | 200m | 500m | 512Mi | 512Mi |

**총 리소스 (최소 구성)**:
- **CPU**: 약 3.5 vCPU (requests 기준)
- **Memory**: 약 5 GiB (requests 기준)

**총 리소스 (최대 스케일)**:
- Frontend: 10 replicas
- Gateway: 10 replicas
- Order: 10 replicas
- 기타: 6-8 replicas
- **예상 최대**: 60+ Pods, 30+ vCPU, 40+ GiB Memory

### 8.4 HPA 모니터링

#### HPA 상태 확인

```bash
# HPA 목록 및 상태
kubectl get hpa -n tori-app

# 특정 HPA 상세 정보
kubectl describe hpa gateway-service-hpa -n tori-app

# 실시간 모니터링
watch kubectl get hpa -n tori-app
```

#### HPA 이벤트 확인

```bash
kubectl get events -n tori-app --sort-by='.lastTimestamp' | grep HorizontalPodAutoscaler
```

#### 메트릭 서버 확인

HPA가 작동하려면 **Metrics Server** 필요:
```bash
# Metrics Server 설치 확인
kubectl get deployment metrics-server -n kube-system

# 설치 (없으면)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### 8.5 수동 스케일링

```bash
# 수동으로 replica 수 조정 (HPA 일시 비활성화됨)
kubectl scale deployment gateway-service --replicas=5 -n tori-app

# HPA 삭제 (수동 관리 전환)
kubectl delete hpa gateway-service-hpa -n tori-app

# HPA 재적용
kubectl apply -f k8s/hpa/hpa.yaml
```

---

## 9. 운영 가이드

### 9.1 배포 절차

#### 새로운 서비스 배포

**1. 코드 변경 및 커밋**
```bash
git add .
git commit -m "feat: add new feature"
git push origin main
```

**2. GitHub Actions 자동 빌드 확인**
- https://github.com/<your-org>/teamprojectv1/actions
- 9개 서비스 Matrix 빌드 완료 대기 (약 5-10분)

**3. K8s 이미지 업데이트**
```bash
# 특정 서비스 재배포 (이미지 pull)
kubectl rollout restart deployment/gateway-service -n tori-app

# 전체 서비스 재배포
kubectl rollout restart deployment -n tori-app
```

**4. 배포 상태 확인**
```bash
# 롤아웃 상태 모니터링
kubectl rollout status deployment/gateway-service -n tori-app

# Pod 상태 확인
kubectl get pods -n tori-app -w
```

#### 특정 이미지 버전으로 배포

```bash
# 이미지 태그 변경
kubectl set image deployment/gateway-service \
  gateway-service=490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/gateway-service:v1.2.0 \
  -n tori-app

# 또는 Deployment YAML 직접 수정
kubectl edit deployment gateway-service -n tori-app
```

### 9.2 롤백 절차

```bash
# 이전 버전으로 롤백
kubectl rollout undo deployment/gateway-service -n tori-app

# 특정 revision으로 롤백
kubectl rollout history deployment/gateway-service -n tori-app
kubectl rollout undo deployment/gateway-service --to-revision=2 -n tori-app
```

### 9.3 데이터베이스 접근

#### 로컬 (Docker Compose)

```bash
# MySQL 컨테이너 접속
docker exec -it mysql-member mysql -u root -p
# Password: rootpass

# 데이터베이스 확인
SHOW DATABASES;
USE member_db;
SHOW TABLES;
```

#### Kubernetes

```bash
# MySQL Pod 접속
kubectl exec -it mysql-member-0 -n tori-app -- mysql -u root -p
# Password: (k8s/base/secrets.yaml의 MYSQL_MEMBER_ROOT_PASSWORD)

# 또는 Port Forward로 로컬에서 접속
kubectl port-forward svc/mysql-member 3306:3306 -n tori-app
mysql -h 127.0.0.1 -u member_user -p member_db
```

### 9.4 RabbitMQ 관리

#### Management UI 접속

**로컬**:
- URL: http://localhost:15672
- 사용자: guest / guest

**Kubernetes**:
```bash
# Port Forward
kubectl port-forward svc/rabbitmq 15672:15672 -n tori-app

# 브라우저 접속
# http://localhost:15672
# 사용자: (secrets.yaml의 RABBITMQ_USER/RABBITMQ_PASSWORD)
```

#### 큐 및 메시지 확인

1. Management UI → Queues 탭
2. `inventory.order.queue` 확인
3. 메시지 수, 소비 속도 모니터링

### 9.5 로그 수집 및 분석

#### 에러 로그 검색

```bash
# 특정 서비스 에러 로그
kubectl logs deployment/gateway-service -n tori-app | grep -i error

# 모든 서비스 에러 로그
for svc in gateway-service member-service product-service order-service; do
  echo "=== $svc ==="
  kubectl logs deployment/$svc -n tori-app --tail=100 | grep -i error
done
```

#### 로그 파일 저장

```bash
# 날짜별 로그 백업
kubectl logs deployment/gateway-service -n tori-app > gateway-$(date +%Y%m%d).log
```

### 9.6 성능 튜닝

#### JVM 옵션 설정

**Deployment에 JVM 옵션 추가**:
```yaml
env:
  - name: JAVA_OPTS
    value: "-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

**Dockerfile Entrypoint 수정**:
```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "app.jar"]
```

#### Connection Pool 튜닝

**application.properties**:
```properties
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

---

## 10. 미구성 인프라

### 10.1 Infrastructure as Code (IaC)

#### ❌ Terraform / CloudFormation

**현재 상태**: K8s 매니페스트는 YAML로 관리, 클라우드 인프라는 수동 구성

**권장 구성**:
- **Terraform**: EKS 클러스터, VPC, IAM 역할, ECR 레포지토리 등 관리
- **eksctl**: EKS 클러스터 자동 프로비저닝

**예시 Terraform 구조** (미구성):
```
terraform/
├── main.tf              # Provider 설정 (AWS)
├── vpc.tf               # VPC, 서브넷, NAT Gateway
├── eks.tf               # EKS 클러스터
├── ecr.tf               # ECR 레포지토리 (9개)
├── rds.tf               # RDS MySQL (대안: StatefulSet 대신)
├── elasticache.tf       # ElastiCache Redis (세션 스토어)
└── variables.tf         # 변수 정의
```

### 10.2 GitOps (ArgoCD / Flux CD)

#### ❌ ArgoCD

**현재 상태**: K8s 배포는 수동 `kubectl apply`

**권장 구성**:
- ArgoCD 설치
- Git Repository를 Single Source of Truth로 설정
- 자동 동기화 및 롤백

**ArgoCD 설치** (미구성):
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# ArgoCD UI 접속
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

### 10.3 Service Mesh

#### ❌ Istio / Linkerd

**현재 상태**: 서비스 간 통신은 K8s Service DNS 사용

**Service Mesh 장점**:
- mTLS 자동 암호화
- Traffic Routing (Canary, Blue/Green)
- Observability (Distributed Tracing)
- Circuit Breaker, Retry, Timeout

**권장**: 초기 단계에서는 불필요, 서비스 수가 20개 이상일 때 고려

### 10.4 Backup & Disaster Recovery

#### ❌ Database Backup

**현재 상태**: PVC에 데이터 저장, 백업 전략 없음

**권장 구성**:
- **Velero**: K8s 리소스 및 PVC 백업
- **AWS Backup**: EBS 스냅샷 자동화
- **MySQL Dump**: Cron Job으로 매일 S3에 백업

**Velero 백업 예시** (미구성):
```bash
# Velero 설치
velero install --provider aws --bucket tori-backup --secret-file ./credentials-velero

# 백업 생성
velero backup create tori-backup-$(date +%Y%m%d) --include-namespaces tori-app

# 복원
velero restore create --from-backup tori-backup-20240415
```

#### ❌ Multi-Region Deployment

**현재 상태**: 단일 리전 (ap-northeast-2)

**권장**:
- **Multi-Region EKS**: 리전 장애 대비
- **RDS Cross-Region Replica**: 데이터베이스 복제
- **Global Accelerator**: 글로벌 트래픽 라우팅

### 10.5 Security

#### ❌ Network Policy

**현재 상태**: 모든 Pod 간 통신 허용

**권장 구성**:
```yaml
# network-policy.yaml (미구성)
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-gateway-to-services
  namespace: tori-app
spec:
  podSelector:
    matchLabels:
      tier: backend
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: gateway-service
```

#### ❌ Secrets Management (AWS Secrets Manager)

**현재 상태**: K8s Secrets (Base64 인코딩만)

**권장**:
- **AWS Secrets Manager** + **External Secrets Operator**
- **Sealed Secrets**: Git에 암호화된 Secrets 커밋

#### ❌ Image Scanning

**현재 상태**: Docker 이미지 보안 스캔 없음

**권장**:
- **AWS ECR Image Scanning**: 취약점 자동 검사
- **Trivy**: CI/CD에서 이미지 스캔

### 10.6 Cost Optimization

#### ❌ Spot Instances

**현재 상태**: On-Demand EKS 노드

**권장**:
- **EC2 Spot Instances**: 비용 최대 90% 절감
- **Karpenter**: 노드 자동 프로비저닝 및 Spot 활용

#### ❌ Resource Optimization

**현재 상태**: 수동 리소스 설정

**권장**:
- **Vertical Pod Autoscaler (VPA)**: 리소스 자동 최적화
- **Goldilocks**: VPA 권장사항 시각화

---

## 부록 A: 트러블슈팅

### A.1 Docker Compose

**문제**: 서비스가 시작되지 않음
```bash
# 해결: 의존성 health check 확인
docker-compose ps
docker-compose logs mysql-member
```

**문제**: Port already allocated
```bash
# 해결: 기존 컨테이너 제거
docker-compose down
docker ps -a | grep 3306
docker rm -f <container-id>
```

### A.2 Kubernetes

**문제**: Pod가 ImagePullBackOff
```bash
# 해결: ECR 인증 확인
kubectl describe pod <pod-name> -n tori-app
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com
```

**문제**: Pod가 CrashLoopBackOff
```bash
# 해결: 로그 확인
kubectl logs <pod-name> -n tori-app --previous
kubectl describe pod <pod-name> -n tori-app
```

**문제**: Service가 응답하지 않음
```bash
# 해결: Endpoints 확인
kubectl get endpoints gateway-service -n tori-app
kubectl get pods -l app=gateway-service -n tori-app -o wide
```

---

## 부록 B: 참고 자료

### B.1 공식 문서

- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [AWS EKS User Guide](https://docs.aws.amazon.com/eks/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)

### B.2 유용한 도구

| 도구 | 용도 | 설치 |
|------|------|------|
| kubectl | K8s 클러스터 관리 | `choco install kubernetes-cli` |
| helm | K8s 패키지 매니저 | `choco install kubernetes-helm` |
| k9s | K8s TUI | `choco install k9s` |
| stern | 멀티 Pod 로그 | `choco install stern` |
| kubectx/kubens | Context/Namespace 전환 | `choco install kubens` |

---

**문서 버전**: 1.0
**최종 업데이트**: 2026-04-15
**작성 기준**: 실제 파일 분석 (추측 최소화)