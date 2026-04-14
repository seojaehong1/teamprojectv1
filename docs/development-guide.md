# Development Guide

> MSAproject_park 프로젝트 개발 가이드
> 실제 코드 기반으로 작성된 구체적인 개발 절차 및 규칙

## 목차

1. [새 API 엔드포인트 추가 시 체크리스트](#1-새-api-엔드포인트-추가-시-체크리스트)
2. [새 서비스/모듈 추가 절차](#2-새-서비스모듈-추가-절차)
3. [DB 스키마 변경 프로세스](#3-db-스키마-변경-프로세스)
4. [환경변수 추가 시 수정 파일 목록](#4-환경변수-추가-시-수정-파일-목록)
5. [테스트 작성 규칙](#5-테스트-작성-규칙)
6. [서비스 간 통신 규칙](#6-서비스-간-통신-규칙)
7. [일반적인 개발 워크플로우](#7-일반적인-개발-워크플로우)
8. [트러블슈팅](#8-트러블슈팅)
9. [서비스 포트 맵](#9-서비스-포트-맵)
10. [서비스 간 의존성 맵](#10-서비스-간-의존성-맵)

---

## 1. 새 API 엔드포인트 추가 시 체크리스트

### 1.1 수정해야 할 파일 목록

#### 필수 파일 (모든 엔드포인트)

1. **Controller** - `src/main/java/{package}/controller/`
2. **Service** - `src/main/java/{package}/service/`
3. **Repository** - `src/main/java/{package}/repository/`
4. **Request DTO** - `src/main/java/{package}/dto/request/`
5. **Response DTO** - `src/main/java/{package}/dto/response/`

#### 조건부 파일

6. **Entity** (새 테이블 필요 시) - `src/main/java/{package}/model/` or `entity/`
7. **Gateway 라우팅** (외부 접근 필요 시) - `gateway-service/src/main/resources/application.properties`
8. **K8s ConfigMap** (환경변수 필요 시) - `k8s/base/configmap.yaml`
9. **테스트 코드** - `src/test/java/{package}/`

### 1.2 실제 예시: 회원 프로필 조회 API 추가

#### Step 1: Request/Response DTO 정의

**파일**: `member-service/src/main/java/com/example/member/dto/request/UpdateProfileRequest.java`
```java
package com.example.member.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String email;
}
```

**파일**: `member-service/src/main/java/com/example/member/dto/response/ProfileResponse.java`
```java
package com.example.member.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String userId;
    private String name;
    private String email;
    private String userType;
    private String createdAt;
}
```

#### Step 2: Service 메서드 추가

**파일**: `member-service/src/main/java/com/example/member/service/MemberService.java`
```java
public Member getMemberByUserId(String userId) {
    return memberRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
}

public Member updateProfile(String userId, String name, String email) {
    Member member = getMemberByUserId(userId);

    // 이메일 중복 체크
    if (memberRepository.existsByEmail(email) && !email.equals(member.getEmail())) {
        throw new RuntimeException("DUPLICATE_EMAIL");
    }

    member.setName(name);
    member.setEmail(email);
    return memberRepository.save(member);
}
```

#### Step 3: Repository 인터페이스 추가

**파일**: `member-service/src/main/java/com/example/member/repository/MemberRepository.java`
```java
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUserId(String userId);
    boolean existsByEmail(String email);
}
```

#### Step 4: Controller 엔드포인트 추가

**파일**: `member-service/src/main/java/com/example/member/controller/UserController.java`
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    public UserController(MemberService memberService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED"));
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED"));
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            Member member = memberService.getMemberByUserId(userId);

            ProfileResponse data = ProfileResponse.builder()
                    .userId(member.getUserId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .userType(member.getUserType())
                    .createdAt(member.getCreatedAt() != null
                            ? member.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                            : null)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("회원정보 조회 성공", data));

        } catch (RuntimeException e) {
            if ("USER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
}
```

#### Step 5: Gateway 라우팅 추가 (외부 접근 필요 시)

**파일**: `gateway-service/src/main/resources/application.properties`
```properties
# 기존 member-service 라우트가 이미 존재하면 생략 가능
spring.cloud.gateway.routes[3].id=member-service
spring.cloud.gateway.routes[3].uri=lb://member-service
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/users/**
```

#### Step 6: 테스트 코드 작성

**파일**: `member-service/src/test/java/com/example/member/controller/UserControllerTest.java`
```java
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_Success() {
        // given
        String token = "valid.jwt.token";
        String userId = "testUser";

        Member member = Member.builder()
                .userId(userId)
                .name("테스트유저")
                .email("test@example.com")
                .userType("member")
                .createdAt(LocalDateTime.now())
                .build();

        when(jwtUtil.validateAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(memberService.getMemberByUserId(userId)).thenReturn(member);

        // when
        ResponseEntity<ApiResponse<?>> response = userController.getProfile("Bearer " + token);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(memberService).getMemberByUserId(userId);
    }
}
```

#### Step 7: 로컬 테스트

```bash
# 1. 빌드
./gradlew :member-service:build

# 2. Docker Compose로 실행
docker-compose up member-service mysql-member eureka-server

# 3. API 테스트
curl -X GET http://localhost:8004/api/users/profile \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### 1.3 체크리스트 요약

```
☐ 1. Request DTO 생성 (필요 시)
☐ 2. Response DTO 생성
☐ 3. Repository 메서드 추가 (필요 시)
☐ 4. Service 메서드 구현
☐ 5. Controller 엔드포인트 추가
☐ 6. Gateway 라우팅 확인/추가
☐ 7. 테스트 코드 작성
☐ 8. 로컬 실행 및 테스트
☐ 9. API 문서 업데이트 (docs/api-spec.md)
```

---

## 2. 새 서비스/모듈 추가 절차

### 2.1 전체 절차 (Step by Step)

#### 예시: notification-service 추가

### Step 1: Gradle 프로젝트 생성

**파일**: `settings.gradle`
```gradle
rootProject.name = 'MSAproject_park'

include 'gateway-service'
include 'eureka-server'
include 'product-service'
include 'order-service'
include 'member-service'
include 'board-service'
include 'inventory-service'
include 'frontend-service'
include 'admin-service'
include 'notification-service'  // 추가
```

**파일**: `notification-service/build.gradle`
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.5'
    id 'io.spring.dependency-management' version '1.1.3'
}

group = 'com.example'
version = '1.0.0'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

ext {
    springCloudVersion = '2022.0.4'
}

dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Eureka Client
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'

    // MySQL
    runtimeOnly 'com.mysql:mysql-connector-j'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### Step 2: application.properties 설정

**파일**: `notification-service/src/main/resources/application.properties`
```properties
server.port=8009
spring.application.name=notification-service

# Eureka Client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# MySQL Database
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3310/notification_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:rootpass}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Actuator
management.endpoints.web.exposure.include=prometheus,health,info
management.endpoint.prometheus.enabled=true
management.endpoints.web.base-path=/actuator
```

### Step 3: Main Application 클래스

**파일**: `notification-service/src/main/java/com/example/notification/NotificationServiceApplication.java`
```java
package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

### Step 4: Dockerfile 생성

**파일**: `notification-service/Dockerfile`
```dockerfile
# Multi-stage build
FROM gradle:8-jdk17-alpine AS builder
WORKDIR /app

# Gradle 빌드
COPY build.gradle settings.gradle ./
COPY notification-service/build.gradle notification-service/
COPY notification-service/src notification-service/src

RUN gradle :notification-service:build -x test --no-daemon

# Runtime 이미지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/notification-service/build/libs/*.jar app.jar

EXPOSE 8009
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 5: docker-compose.yml 추가

**파일**: `docker-compose.yml`
```yaml
services:
  # 기존 서비스들...

  # MySQL - Notification DB
  mysql-notification:
    image: mysql:8.0
    container_name: mysql-notification
    ports:
      - "3310:3306"
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: notification_db
      MYSQL_USER: notification_user
      MYSQL_PASSWORD: notification_pass
    volumes:
      - mysql-notification-data:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpass"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - tori-network

  # Notification Service
  notification-service:
    build:
      context: .
      dockerfile: notification-service/Dockerfile
    container_name: notification-service
    ports:
      - "8009:8009"
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-notification:3306/notification_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
      SPRING_DATASOURCE_USERNAME: notification_user
      SPRING_DATASOURCE_PASSWORD: notification_pass
    depends_on:
      eureka-server:
        condition: service_healthy
      mysql-notification:
        condition: service_healthy
    networks:
      - tori-network

volumes:
  # 기존 volumes...
  mysql-notification-data:
```

### Step 6: Gateway 라우팅 추가

**파일**: `gateway-service/src/main/resources/application.properties`
```properties
# 기존 라우트들...

# Notification Service
spring.cloud.gateway.routes[17].id=notification-service
spring.cloud.gateway.routes[17].uri=lb://notification-service
spring.cloud.gateway.routes[17].predicates[0]=Path=/api/notifications/**
```

### Step 7: Kubernetes 매니페스트 생성

**파일**: `k8s/mysql/mysql-notification.yaml`
```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-notification
  namespace: tori-app
spec:
  ports:
    - port: 3306
  selector:
    app: mysql-notification
  clusterIP: None
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql-notification
  namespace: tori-app
spec:
  serviceName: mysql-notification
  replicas: 1
  selector:
    matchLabels:
      app: mysql-notification
  template:
    metadata:
      labels:
        app: mysql-notification
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
              name: mysql-secret
              key: mysql-root-password
        - name: MYSQL_DATABASE
          value: notification_db
        - name: MYSQL_USER
          value: notification_user
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: mysql-notification-password
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
  volumeClaimTemplates:
  - metadata:
      name: mysql-storage
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 10Gi
```

**파일**: `k8s/services/notification-service.yaml`
```yaml
apiVersion: v1
kind: Service
metadata:
  name: notification-service
  namespace: tori-app
spec:
  ports:
    - port: 8009
      targetPort: 8009
  selector:
    app: notification-service
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
  namespace: tori-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
    spec:
      containers:
      - name: notification-service
        image: 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/notification-service:latest
        ports:
        - containerPort: 8009
        env:
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
        - name: SPRING_DATASOURCE_URL
          value: jdbc:mysql://mysql-notification:3306/notification_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
        - name: SPRING_DATASOURCE_USERNAME
          value: notification_user
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: mysql-notification-password
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8009
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8009
          initialDelaySeconds: 30
          periodSeconds: 5
```

### Step 8: HPA 설정 추가

**파일**: `k8s/hpa/hpa.yaml`
```yaml
# 기존 HPA들...

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: notification-service-hpa
  namespace: tori-app
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: notification-service
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
```

### Step 9: CI/CD 파이프라인 추가

**파일**: `.github/workflows/ci.yml`
```yaml
jobs:
  build:
    runs-on: ubuntu-latest

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
          - notification-service  # 추가
```

### Step 10: K8s Secrets 업데이트

**파일**: `k8s/base/secrets.yaml`
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-secret
  namespace: tori-app
type: Opaque
stringData:
  mysql-root-password: "rootpass"
  mysql-member-password: "member_pass"
  mysql-product-password: "product_pass"
  mysql-order-password: "order_pass"
  mysql-inventory-password: "inventory_pass"
  mysql-notification-password: "notification_pass"  # 추가
```

### Step 11: 로컬 테스트

```bash
# 1. Gradle 빌드
./gradlew :notification-service:build

# 2. Docker Compose로 실행
docker-compose up notification-service mysql-notification eureka-server

# 3. Eureka 대시보드 확인
# http://localhost:8761 접속 후 notification-service 등록 확인

# 4. Health Check
curl http://localhost:8009/actuator/health
```

### Step 12: K8s 배포

```bash
# 1. ECR 이미지 푸시 (CI/CD 트리거)
git add .
git commit -m "feat: Add notification-service"
git push origin main

# 2. K8s 리소스 배포
kubectl apply -f k8s/mysql/mysql-notification.yaml
kubectl apply -f k8s/services/notification-service.yaml
kubectl apply -f k8s/hpa/hpa.yaml

# 3. Pod 상태 확인
kubectl get pods -n tori-app -l app=notification-service

# 4. 서비스 확인
kubectl get svc -n tori-app notification-service
```

### 2.2 체크리스트 요약

```
☐ 1. settings.gradle에 모듈 추가
☐ 2. build.gradle 생성
☐ 3. application.properties 설정
☐ 4. Main Application 클래스 생성
☐ 5. Dockerfile 생성
☐ 6. docker-compose.yml에 서비스 추가
☐ 7. Gateway 라우팅 추가
☐ 8. K8s MySQL StatefulSet 생성
☐ 9. K8s Deployment/Service 생성
☐ 10. K8s HPA 설정 추가
☐ 11. CI/CD 파이프라인에 서비스 추가
☐ 12. K8s Secrets 업데이트
☐ 13. 로컬 테스트 (Docker Compose)
☐ 14. K8s 배포 및 검증
```

---

## 3. DB 스키마 변경 프로세스

### 3.1 현재 방식: Hibernate ddl-auto=update

**설정 파일**: 모든 서비스의 `application.properties`
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**동작 방식**:
- 서비스 시작 시 Entity 클래스를 읽어 자동으로 DDL 실행
- 컬럼 추가는 자동, 삭제는 수동 필요
- 롤백 불가능

### 3.2 스키마 변경 절차

#### Case 1: 새 컬럼 추가

**Step 1: Entity 수정**

**파일**: `member-service/src/main/java/com/example/member/model/Member.java`
```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", unique = true, nullable = false, length = 50)
    private String userId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "user_type", length = 20)
    private String userType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 새 컬럼 추가
    @Column(name = "nickname", length = 50)
    private String nickname;  // 추가
}
```

**Step 2: 서비스 재시작**

```bash
# Docker Compose 환경
docker-compose restart member-service

# Kubernetes 환경
kubectl rollout restart deployment/member-service -n tori-app
```

**Step 3: 로그 확인**

```
Hibernate: alter table users add column nickname varchar(50)
```

#### Case 2: 컬럼 삭제 (수동 DDL 필요)

**문제**: Entity에서 필드를 제거해도 DB 컬럼은 남아있음

**Step 1: Entity에서 필드 제거**
```java
// @Column(name = "phone")
// private String phone;  // 삭제
```

**Step 2: 수동 DDL 실행**

```sql
-- 로컬 환경
mysql -u root -prootpass -h localhost -P 3306 member_db
ALTER TABLE users DROP COLUMN phone;

-- K8s 환경
kubectl exec -it mysql-member-0 -n tori-app -- mysql -u root -prootpass member_db
ALTER TABLE users DROP COLUMN phone;
```

#### Case 3: 컬럼명 변경 (주의 필요)

**잘못된 방법**:
```java
// Before
private String phone;

// After - 필드명만 변경
private String phoneNumber;  // ❌ 새 컬럼 생성됨, 데이터 이동 안 됨
```

**올바른 방법**:
```java
@Column(name = "phone")  // DB 컬럼명 유지
private String phoneNumber;  // Java 필드명만 변경
```

**완전히 변경하려면 수동 DDL**:
```sql
ALTER TABLE users CHANGE COLUMN phone phone_number VARCHAR(20);
```

#### Case 4: NOT NULL 제약조건 추가

**문제**: 기존 행에 NULL 값이 있으면 에러 발생

**Step 1: nullable=true로 컬럼 추가**
```java
@Column(name = "nickname", length = 50)
private String nickname;
```

**Step 2: 기존 데이터에 기본값 설정**
```sql
UPDATE users SET nickname = CONCAT('user', user_id) WHERE nickname IS NULL;
```

**Step 3: NOT NULL 제약조건 추가**
```java
@Column(name = "nickname", nullable = false, length = 50)
private String nickname;
```

**Step 4: 서비스 재시작**
```bash
docker-compose restart member-service
```

### 3.3 주의사항

#### ⚠️ 1. 프로덕션에서 ddl-auto=update 사용 시 위험

**문제점**:
- 예상치 못한 스키마 변경
- 롤백 불가능
- 대용량 테이블 변경 시 락 발생
- 데이터 손실 위험

**권장 방법**: Flyway 또는 Liquibase 사용

**Flyway 예시**:

**파일**: `member-service/build.gradle`
```gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
}
```

**파일**: `member-service/src/main/resources/application.properties`
```properties
# Hibernate ddl-auto 비활성화
spring.jpa.hibernate.ddl-auto=validate

# Flyway 활성화
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**파일**: `member-service/src/main/resources/db/migration/V1__init_schema.sql`
```sql
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    user_type VARCHAR(20),
    created_at TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_email (email)
);
```

**파일**: `member-service/src/main/resources/db/migration/V2__add_nickname.sql`
```sql
ALTER TABLE users ADD COLUMN nickname VARCHAR(50);
UPDATE users SET nickname = CONCAT('user', user_id) WHERE nickname IS NULL;
ALTER TABLE users MODIFY COLUMN nickname VARCHAR(50) NOT NULL;
```

#### ⚠️ 2. 인덱스 추가

**Entity에 인덱스 추가**:
```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Member {
    // ...
}
```

**Hibernate가 자동으로 인덱스 생성**:
```sql
CREATE INDEX idx_user_id ON users(user_id);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_created_at ON users(created_at);
```

#### ⚠️ 3. 외래키 관계 변경

**Entity 관계 추가**:
```java
@Entity
public class Orders {
    // ...

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}

@Entity
public class OrderItem {
    // ...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;
}
```

**Hibernate가 자동으로 외래키 생성**:
```sql
ALTER TABLE order_items
ADD CONSTRAINT fk_order_id
FOREIGN KEY (order_id) REFERENCES orders(order_id);
```

### 3.4 체크리스트

```
☐ 1. 로컬 환경에서 먼저 테스트
☐ 2. Entity 클래스 수정
☐ 3. 컬럼 삭제/변경 시 수동 DDL 준비
☐ 4. NOT NULL 추가 시 기존 데이터 처리
☐ 5. 서비스 재시작 후 로그 확인
☐ 6. 롤백 DDL 준비 (변경 사항 백업)
☐ 7. 프로덕션 배포 전 스테이징 검증
```

---

## 4. 환경변수 추가 시 수정 파일 목록

### 4.1 환경변수 계층 구조

```
1. application.properties (기본값)
2. .env 파일 (로컬 개발)
3. docker-compose.yml (Docker Compose 환경)
4. K8s ConfigMap/Secrets (프로덕션)
```

### 4.2 수정해야 할 파일

#### 예시: 새 환경변수 `NOTIFICATION_API_KEY` 추가

#### Layer 1: application.properties (기본값 정의)

**파일**: `notification-service/src/main/resources/application.properties`
```properties
# Notification API Key
notification.api.key=${NOTIFICATION_API_KEY:default-api-key-for-dev}
```

#### Layer 2: .env 파일 (로컬 개발)

**파일**: `.env` (프로젝트 루트)
```env
# Member Service
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters-long
JWT_EXPIRATION=86400000
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Notification Service
NOTIFICATION_API_KEY=local-dev-api-key-12345
```

**주의**: `.env` 파일은 `.gitignore`에 포함 (민감정보 보호)

**파일**: `.gitignore`
```
.env
*.env
```

**파일**: `.env.example` (템플릿 제공)
```env
# Member Service
JWT_SECRET=your-jwt-secret-here
JWT_EXPIRATION=86400000
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Notification Service
NOTIFICATION_API_KEY=your-notification-api-key
```

#### Layer 3: docker-compose.yml (Docker Compose 환경)

**파일**: `docker-compose.yml`
```yaml
services:
  notification-service:
    build:
      context: .
      dockerfile: notification-service/Dockerfile
    container_name: notification-service
    ports:
      - "8009:8009"
    env_file:
      - .env  # .env 파일에서 로드
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-notification:3306/notification_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
      SPRING_DATASOURCE_USERNAME: notification_user
      SPRING_DATASOURCE_PASSWORD: notification_pass
      NOTIFICATION_API_KEY: ${NOTIFICATION_API_KEY}  # .env에서 주입
    depends_on:
      eureka-server:
        condition: service_healthy
      mysql-notification:
        condition: service_healthy
    networks:
      - tori-network
```

#### Layer 4: K8s ConfigMap (비밀 아닌 설정)

**파일**: `k8s/base/configmap.yaml`
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: tori-app
data:
  # Eureka
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://eureka-server:8761/eureka/"

  # Service URLs (frontend-service용)
  SERVICE_MEMBER_URL: "http://member-service:8004"
  SERVICE_BOARD_URL: "http://board-service:8006"
  SERVICE_ADMIN_URL: "http://admin-service:8007"
  SERVICE_PRODUCT_URL: "http://product-service:8001"
  SERVICE_ORDER_URL: "http://order-service:8002"
  SERVICE_INVENTORY_URL: "http://inventory-service:8008"
  SERVICE_NOTIFICATION_URL: "http://notification-service:8009"  # 추가

  # RabbitMQ
  RABBITMQ_HOST: "rabbitmq"
  RABBITMQ_PORT: "5672"
  RABBITMQ_USERNAME: "guest"
```

#### Layer 5: K8s Secrets (민감정보)

**파일**: `k8s/base/secrets.yaml`
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secret
  namespace: tori-app
type: Opaque
stringData:
  # JWT
  JWT_SECRET: "your-production-jwt-secret-must-be-at-least-32-characters-long"
  JWT_EXPIRATION: "86400000"

  # OAuth2
  OAUTH2_GOOGLE_CLIENT_ID: "your-google-client-id"
  OAUTH2_GOOGLE_CLIENT_SECRET: "your-google-client-secret"
  OAUTH2_KAKAO_CLIENT_ID: "your-kakao-client-id"
  OAUTH2_KAKAO_CLIENT_SECRET: "your-kakao-client-secret"

  # Email
  MAIL_USERNAME: "your-email@gmail.com"
  MAIL_PASSWORD: "your-app-password"

  # RabbitMQ
  RABBITMQ_PASSWORD: "guest"

  # Notification (추가)
  NOTIFICATION_API_KEY: "prod-notification-api-key-67890"
```

#### Layer 6: K8s Deployment (환경변수 주입)

**파일**: `k8s/services/notification-service.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
  namespace: tori-app
spec:
  template:
    spec:
      containers:
      - name: notification-service
        image: 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/notification-service:latest
        env:
        # ConfigMap에서 주입
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE

        # Secret에서 주입
        - name: NOTIFICATION_API_KEY
          valueFrom:
            secretKeyRef:
              name: app-secret
              key: NOTIFICATION_API_KEY

        # 직접 정의
        - name: SPRING_DATASOURCE_URL
          value: jdbc:mysql://mysql-notification:3306/notification_db?useSSL=false
```

### 4.3 환경변수 적용 순서

```bash
# 1. application.properties 수정 (기본값 정의)
vim notification-service/src/main/resources/application.properties

# 2. .env 파일 수정 (로컬 개발)
vim .env

# 3. docker-compose.yml 수정 (Docker Compose 환경)
vim docker-compose.yml

# 4. K8s ConfigMap/Secrets 수정 (프로덕션)
vim k8s/base/configmap.yaml
vim k8s/base/secrets.yaml

# 5. K8s Deployment 수정 (환경변수 주입 설정)
vim k8s/services/notification-service.yaml

# 6. K8s 적용
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml
kubectl rollout restart deployment/notification-service -n tori-app
```

### 4.4 환경변수 분류 기준

| 환경변수 유형 | 저장 위치 | 예시 |
|--------------|----------|------|
| **비밀정보** | K8s Secrets | JWT_SECRET, DB 비밀번호, API Key |
| **서비스 URL** | K8s ConfigMap | EUREKA URL, Service URLs |
| **포트/호스트** | K8s ConfigMap | RABBITMQ_HOST, RABBITMQ_PORT |
| **DB 연결정보** | Deployment (직접) | SPRING_DATASOURCE_URL |
| **개발용 기본값** | application.properties | 모든 변수의 기본값 |

### 4.5 체크리스트

```
☐ 1. application.properties에 기본값 추가
☐ 2. .env.example에 템플릿 추가
☐ 3. .env 파일에 로컬 개발용 값 추가
☐ 4. docker-compose.yml에 환경변수 주입 설정
☐ 5. K8s ConfigMap 또는 Secrets에 추가 (민감정보 여부 판단)
☐ 6. K8s Deployment에 환경변수 주입 설정
☐ 7. 로컬 테스트 (docker-compose up)
☐ 8. K8s 배포 및 검증
```

---

## 5. 테스트 작성 규칙

### 5.1 현재 프로젝트의 테스트 프레임워크

**기술 스택**:
- JUnit 5 (Jupiter)
- Mockito (Mock 객체)
- AssertJ (Assertion)
- Spring Boot Test

**의존성**: `build.gradle`
```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### 5.2 테스트 패턴

#### Pattern 1: Service Layer 테스트 (가장 일반적)

**파일**: `order-service/src/test/java/com/example/cust/service/OrderServiceTest.java`
```java
package com.example.cust.service;

import com.example.cust.config.RabbitConfig;
import com.example.cust.model.*;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private CartDetailService cartDetailService;

    @Mock
    private CartHeaderRepository cartHeaderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private CartHeader testCartHeader;
    private CartItem testCartItem;
    private Orders testOrder;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        CartOption cartOption = CartOption.builder()
                .optionId(1)
                .optionName("샷 추가")
                .optionPrice(500)
                .build();

        testCartItem = CartItem.builder()
                .cartItemId(1)
                .menuCode("COFFEE001")
                .menuName("아메리카노")
                .quantity(2)
                .unitPrice(4500)
                .totalItemPrice(10000)
                .cartOptions(new ArrayList<>(List.of(cartOption)))
                .build();

        testCartHeader = CartHeader.builder()
                .cartId(1)
                .customerId("testUser")
                .createdAt(LocalDateTime.now())
                .cartItems(new ArrayList<>(List.of(testCartItem)))
                .build();

        testOrder = Orders.builder()
                .orderId(1)
                .customerId("testUser")
                .customerName("테스트유저")
                .orderDate(LocalDateTime.now())
                .totalAmount(10000)
                .status(OrderStatus.PENDING)
                .request("얼음 많이 주세요")
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공 - 장바구니 아이템이 주문으로 변환되어야 한다")
    void placeOrder_Success() {
        // given
        String customerId = "testUser";
        String customerName = "테스트유저";
        String requestMessage = "얼음 많이 주세요";

        when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(testCartHeader);
        when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setOrderId(1);
            return order;
        });
        doNothing().when(cartHeaderRepository).delete(any(CartHeader.class));

        // when
        Orders result = orderService.placeOrder(customerId, customerName, requestMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getCustomerName()).isEqualTo(customerName);
        assertThat(result.getRequest()).isEqualTo(requestMessage);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualTo(10000);
        assertThat(result.getOrderItems()).hasSize(1);

        // RabbitMQ 메시지 전송 확인
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.ORDER_EXCHANGE),
                eq(RabbitConfig.ORDER_PLACED_ROUTING_KEY),
                any()
        );

        // 장바구니 삭제 확인
        verify(cartHeaderRepository).delete(testCartHeader);
    }

    @Test
    @DisplayName("주문 생성 실패 - 빈 장바구니일 경우 예외 발생")
    void placeOrder_EmptyCart_ThrowsException() {
        // given
        String customerId = "testUser";
        CartHeader emptyCartHeader = CartHeader.builder()
                .cartId(1)
                .customerId(customerId)
                .cartItems(new ArrayList<>())
                .build();

        when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(emptyCartHeader);

        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(customerId, "테스트", "요청사항"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니가 비어있습니다.");

        // 주문이 저장되지 않았는지 확인
        verify(ordersRepository, never()).save(any());
    }
}
```

#### Pattern 2: Util 클래스 테스트

**파일**: `member-service/src/test/java/com/example/member/util/JwtUtilTest.java`
```java
package com.example.member.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // ReflectionTestUtils로 private 필드 주입
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "test-secret-key-must-be-at-least-32-characters-long-for-hs512");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L);  // 1시간
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L);  // 7일

        jwtUtil.init();  // @PostConstruct 메서드 수동 호출
    }

    @Test
    @DisplayName("Access Token 생성 및 검증 성공")
    void generateAndValidateAccessToken_Success() {
        // given
        String userId = "testUser";
        String userType = "member";

        // when
        String token = jwtUtil.generateAccessToken(userId, userType);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtUtil.validateAccessToken(token)).isTrue();
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtUtil.getUserTypeFromToken(token)).isEqualTo(userType);
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateExpiredToken_Failure() {
        // given
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "test-secret-key-must-be-at-least-32-characters-long-for-hs512");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L);  // 이미 만료
        jwtUtil.init();

        String token = jwtUtil.generateAccessToken("testUser", "member");

        // when & then
        assertThat(jwtUtil.validateAccessToken(token)).isFalse();
    }

    @Test
    @DisplayName("블랙리스트 토큰 검증 실패")
    void validateBlacklistedToken_Failure() {
        // given
        String token = jwtUtil.generateAccessToken("testUser", "member");
        jwtUtil.addToBlacklist(token);

        // when & then
        assertThat(jwtUtil.validateAccessToken(token)).isFalse();
    }
}
```

### 5.3 테스트 작성 규칙

#### Rule 1: Given-When-Then 패턴 사용

```java
@Test
@DisplayName("테스트 설명")
void testMethod() {
    // given: 테스트 데이터 준비
    String userId = "testUser";
    when(repository.findById(userId)).thenReturn(Optional.of(user));

    // when: 테스트 대상 메서드 실행
    User result = service.getUser(userId);

    // then: 결과 검증
    assertThat(result).isNotNull();
    assertThat(result.getUserId()).isEqualTo(userId);
}
```

#### Rule 2: @DisplayName에 한글 사용

```java
@Test
@DisplayName("주문 생성 성공 - 장바구니 아이템이 주문으로 변환되어야 한다")
void placeOrder_Success() {
    // ...
}

@Test
@DisplayName("주문 생성 실패 - 빈 장바구니일 경우 예외 발생")
void placeOrder_EmptyCart_ThrowsException() {
    // ...
}
```

#### Rule 3: AssertJ 사용 (Hamcrest 대신)

```java
// ✅ Good: AssertJ
assertThat(result).isNotNull();
assertThat(result.getOrderItems()).hasSize(1);
assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

// ❌ Avoid: JUnit Assertions
assertEquals(OrderStatus.PENDING, result.getStatus());
assertTrue(result.getOrderItems().size() == 1);
```

#### Rule 4: Mockito verify() 사용

```java
// RabbitMQ 메시지 전송 확인
verify(rabbitTemplate).convertAndSend(
        eq(RabbitConfig.ORDER_EXCHANGE),
        eq(RabbitConfig.ORDER_PLACED_ROUTING_KEY),
        any()
);

// 호출되지 않았는지 확인
verify(ordersRepository, never()).save(any());
```

#### Rule 5: @BeforeEach로 공통 테스트 데이터 준비

```java
@BeforeEach
void setUp() {
    testCartItem = CartItem.builder()
            .cartItemId(1)
            .menuCode("COFFEE001")
            .menuName("아메리카노")
            .quantity(2)
            .build();
}
```

#### Rule 6: 예외 테스트

```java
@Test
@DisplayName("주문 조회 실패 - 존재하지 않는 주문")
void getOrder_NotFound_ThrowsException() {
    // given
    Integer orderId = 999;
    when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> orderService.getOrder(orderId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("주문을 찾을 수 없습니다");
}
```

### 5.4 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 서비스만 테스트
./gradlew :member-service:test

# 특정 테스트 클래스만 실행
./gradlew :order-service:test --tests OrderServiceTest

# 테스트 리포트 확인
# build/reports/tests/test/index.html
```

### 5.5 테스트 커버리지

**JaCoCo 플러그인 추가** (선택사항):

**파일**: `build.gradle`
```gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.10"
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }
}
```

```bash
# 테스트 + 커버리지 리포트
./gradlew test jacocoTestReport

# 리포트 확인
# build/reports/jacoco/test/html/index.html
```

### 5.6 체크리스트

```
☐ 1. @ExtendWith(MockitoExtension.class) 사용
☐ 2. @Mock, @InjectMocks 사용
☐ 3. @BeforeEach로 테스트 데이터 준비
☐ 4. Given-When-Then 패턴 준수
☐ 5. @DisplayName에 한글 설명
☐ 6. AssertJ Assertions 사용
☐ 7. verify()로 Mock 호출 확인
☐ 8. 예외 케이스 테스트
☐ 9. 성공/실패 시나리오 모두 테스트
```

---

## 6. 서비스 간 통신 규칙

### 6.1 통신 방식

#### 방식 1: REST API (동기 통신)

**사용 사례**: frontend-service → backend services

**기술**: RestTemplate

**예시**: frontend-service가 member-service 호출

**파일**: `frontend-service/src/main/java/com/toricoffee/frontend/service/MemberApiService.java`
```java
@Service
public class MemberApiService {

    private final RestTemplate restTemplate;

    @Value("${SERVICE_MEMBER_URL}")
    private String memberServiceUrl;

    public MemberApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public MemberInfo getMemberInfo(String userId) {
        String url = memberServiceUrl + "/api/users/" + userId;
        return restTemplate.getForObject(url, MemberInfo.class);
    }
}
```

**환경변수 설정**:
- Docker Compose: `SERVICE_MEMBER_URL=http://member-service:8004`
- K8s ConfigMap: `SERVICE_MEMBER_URL=http://member-service:8004`

#### 방식 2: RabbitMQ 메시지 (비동기 통신)

**사용 사례**: order-service → inventory-service

**현재 구현**: 주문 생성 시 재고 차감 이벤트 발행

##### Publisher (order-service)

**파일**: `order-service/src/main/java/com/example/cust/config/RabbitConfig.java`
```java
@Configuration
public class RabbitConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
```

**파일**: `order-service/src/main/java/com/example/cust/service/OrderService.java`
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final RabbitTemplate rabbitTemplate;

    public Orders placeOrder(String customerId, String customerName, String request) {
        // 주문 저장
        Orders order = ordersRepository.save(newOrder);

        // RabbitMQ 메시지 발행
        OrderPlacedEvent event = OrderPlacedEvent.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .orderItems(orderItems)
            .build();

        rabbitTemplate.convertAndSend(
            RabbitConfig.ORDER_EXCHANGE,
            RabbitConfig.ORDER_PLACED_ROUTING_KEY,
            event
        );

        log.info("주문 이벤트 발행: orderId={}", order.getOrderId());
        return order;
    }
}
```

##### Consumer (inventory-service)

**파일**: `inventory-service/src/main/java/com/example/inventory/config/RabbitConfig.java`
```java
@Configuration
public class RabbitConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_ORDER_QUEUE = "inventory.order.queue";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue inventoryOrderQueue() {
        return new Queue(INVENTORY_ORDER_QUEUE, true);  // durable=true
    }

    @Bean
    public Binding bindingInventoryQueue(Queue inventoryOrderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(inventoryOrderQueue).to(orderExchange).with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
```

**파일**: `inventory-service/src/main/java/com/example/inventory/listener/OrderMessageListener.java`
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageListener {

    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory.order.queue")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("주문 이벤트 수신: orderId={}", event.getOrderId());

        try {
            inventoryService.deductInventory(event.getOrderItems());
            log.info("재고 차감 완료: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("재고 차감 실패: orderId={}, error={}",
                event.getOrderId(), e.getMessage());
            // 재시도 또는 DLQ로 전송
        }
    }
}
```

### 6.2 새 메시지/이벤트 추가 절차

#### 예시: 주문 취소 이벤트 추가

##### Step 1: 이벤트 DTO 정의

**파일**: `order-service/src/main/java/com/example/cust/dto/OrderCancelledEvent.java`
```java
package com.example.cust.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderCancelledEvent {
    private Integer orderId;
    private String customerId;
    private String reason;
    private LocalDateTime cancelledAt;
}
```

##### Step 2: Publisher 수정 (order-service)

**RabbitConfig 수정**:
```java
public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
```

**OrderService 수정**:
```java
public void cancelOrder(Integer orderId, String reason) {
    // 주문 상태 변경
    Orders order = ordersRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
    order.setStatus(OrderStatus.CANCELLED);
    ordersRepository.save(order);

    // 이벤트 발행
    OrderCancelledEvent event = OrderCancelledEvent.builder()
        .orderId(orderId)
        .customerId(order.getCustomerId())
        .reason(reason)
        .cancelledAt(LocalDateTime.now())
        .build();

    rabbitTemplate.convertAndSend(
        RabbitConfig.ORDER_EXCHANGE,
        RabbitConfig.ORDER_CANCELLED_ROUTING_KEY,
        event
    );
}
```

##### Step 3: Consumer 추가 (inventory-service)

**RabbitConfig 수정**:
```java
public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";

@Bean
public Binding orderCancelledBinding() {
    return BindingBuilder
        .bind(inventoryOrderQueue())
        .to(orderExchange())
        .with(ORDER_CANCELLED_ROUTING_KEY);
}
```

**OrderMessageListener 수정**:
```java
@RabbitListener(queues = "inventory.order.queue")
public void handleOrderCancelled(OrderCancelledEvent event) {
    log.info("주문 취소 이벤트 수신: orderId={}", event.getOrderId());

    // 재고 복구
    inventoryService.restoreInventory(event.getOrderId());
}
```

##### Step 4: 환경변수 확인

**docker-compose.yml**:
```yaml
order-service:
  environment:
    SPRING_RABBITMQ_HOST: rabbitmq
    SPRING_RABBITMQ_PORT: 5672
    SPRING_RABBITMQ_USERNAME: guest
    SPRING_RABBITMQ_PASSWORD: guest

inventory-service:
  environment:
    SPRING_RABBITMQ_HOST: rabbitmq
    SPRING_RABBITMQ_PORT: 5672
    SPRING_RABBITMQ_USERNAME: guest
    SPRING_RABBITMQ_PASSWORD: guest
```

##### Step 5: 테스트

```bash
# 1. RabbitMQ Management UI 접속
# http://localhost:15672 (guest/guest)

# 2. Exchange 확인
# order.exchange가 존재하는지 확인

# 3. Queue 확인
# inventory.order.queue가 존재하는지 확인

# 4. Binding 확인
# order.exchange → inventory.order.queue
# Routing Key: order.placed, order.cancelled

# 5. 메시지 발행 테스트
curl -X POST http://localhost:8002/api/orders/1/cancel \
  -H "Content-Type: application/json" \
  -d '{"reason": "고객 요청"}'

# 6. inventory-service 로그 확인
docker-compose logs -f inventory-service
```

### 6.3 통신 규칙 체크리스트

#### REST API 추가 시

```
☐ 1. 환경변수로 서비스 URL 정의 (SERVICE_XXX_URL)
☐ 2. RestTemplate Bean 설정
☐ 3. API 호출 메서드 구현
☐ 4. 에러 처리 (try-catch, 타임아웃)
☐ 5. docker-compose.yml에 환경변수 추가
☐ 6. K8s ConfigMap에 서비스 URL 추가
```

#### RabbitMQ 이벤트 추가 시

```
☐ 1. 이벤트 DTO 정의
☐ 2. Publisher RabbitConfig 수정 (Exchange, RoutingKey)
☐ 3. Publisher 메시지 발행 코드 추가
☐ 4. Consumer RabbitConfig 수정 (Queue, Binding)
☐ 5. Consumer @RabbitListener 메서드 추가
☐ 6. docker-compose.yml에 RabbitMQ 환경변수 확인
☐ 7. RabbitMQ Management UI에서 바인딩 확인
☐ 8. 에러 처리 및 재시도 로직
```

---

## 7. 일반적인 개발 워크플로우

### 7.1 로컬 개발 환경 실행

#### Step 1: 프로젝트 클론 및 의존성 설치

```bash
# 프로젝트 클론
git clone <repository-url>
cd teamprojectv1

# Gradle 빌드 (의존성 다운로드)
./gradlew build -x test
```

#### Step 2: 환경변수 설정

```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 수정 (실제 값 입력)
vim .env
```

**`.env` 예시**:
```env
# JWT
JWT_SECRET=your-secret-key-at-least-32-characters-long
JWT_EXPIRATION=86400000

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# OAuth2 (선택사항)
OAUTH2_GOOGLE_CLIENT_ID=your-google-client-id
OAUTH2_GOOGLE_CLIENT_SECRET=your-google-client-secret
```

#### Step 3: Docker Compose로 전체 환경 실행

```bash
# 전체 서비스 실행
docker-compose up -d

# 특정 서비스만 실행 (예: member-service 개발 시)
docker-compose up -d mysql-member eureka-server gateway-service member-service

# 로그 확인
docker-compose logs -f member-service
```

#### Step 4: 서비스 정상 동작 확인

```bash
# Eureka Dashboard
http://localhost:8761

# Gateway Health Check
curl http://localhost:8000/actuator/health

# Member Service Health Check
curl http://localhost:8004/actuator/health

# RabbitMQ Management UI
http://localhost:15672 (guest/guest)
```

### 7.2 코드 수정 및 테스트

#### Step 1: 코드 수정

```bash
# IDE에서 코드 수정
# 예: member-service/src/main/java/com/example/member/controller/UserController.java
```

#### Step 2: 로컬 빌드 및 테스트

```bash
# 단위 테스트 실행
./gradlew :member-service:test

# 빌드 (테스트 포함)
./gradlew :member-service:build

# 빌드 (테스트 제외)
./gradlew :member-service:build -x test
```

#### Step 3: Docker 이미지 재빌드 및 재시작

```bash
# 특정 서비스만 재빌드
docker-compose build member-service

# 재시작
docker-compose up -d member-service

# 로그 확인
docker-compose logs -f member-service
```

#### Step 4: API 테스트

```bash
# 회원가입
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "password": "password123",
    "name": "테스트유저",
    "email": "test@example.com"
  }'

# 로그인
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "password": "password123"
  }'

# 프로필 조회 (JWT 토큰 필요)
curl -X GET http://localhost:8000/api/users/profile \
  -H "Authorization: Bearer {ACCESS_TOKEN}"
```

### 7.3 Git 브랜치 전략

#### Branch 구조

```
main (프로덕션)
  ↑
develop (개발)
  ↑
feature/xxx (기능 개발)
```

#### 새 기능 개발

```bash
# 1. develop 브랜치에서 feature 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b feature/add-notification-service

# 2. 코드 수정 및 커밋
git add .
git commit -m "feat: Add notification service"

# 3. 원격 브랜치에 push
git push origin feature/add-notification-service

# 4. Pull Request 생성 (feature → develop)
# GitHub에서 PR 생성

# 5. 코드 리뷰 및 승인 후 merge

# 6. develop → main 배포 시 PR 생성
```

#### Commit Message 규칙

```
feat: 새 기능 추가
fix: 버그 수정
refactor: 코드 리팩토링
docs: 문서 수정
test: 테스트 코드 추가/수정
chore: 빌드 설정 변경
```

**예시**:
```
feat: Add user profile update API
fix: Fix JWT token expiration issue
refactor: Extract common JWT validation logic
docs: Update API documentation
test: Add OrderService unit tests
chore: Update Spring Boot to 3.1.6
```

### 7.4 배포 프로세스

#### 로컬 → Docker Compose (개발 환경)

```bash
# 1. 코드 수정

# 2. 테스트
./gradlew :member-service:test

# 3. 빌드
./gradlew :member-service:build

# 4. Docker Compose 재시작
docker-compose up -d --build member-service

# 5. 동작 확인
curl http://localhost:8004/actuator/health
```

#### GitHub → AWS EKS (프로덕션 환경)

```bash
# 1. Pull Request 생성 및 승인
# feature → develop → main

# 2. main 브랜치 push 시 CI/CD 자동 트리거
git push origin main

# 3. GitHub Actions 확인
# .github/workflows/ci.yml
# ECR에 이미지 자동 푸시

# 4. K8s 배포 (수동)
kubectl rollout restart deployment/member-service -n tori-app

# 5. 배포 상태 확인
kubectl rollout status deployment/member-service -n tori-app

# 6. Pod 로그 확인
kubectl logs -f deployment/member-service -n tori-app

# 7. API 테스트
curl http://<INGRESS-URL>/api/users/profile \
  -H "Authorization: Bearer {TOKEN}"
```

### 7.5 워크플로우 체크리스트

```
☐ 1. feature 브랜치 생성
☐ 2. 코드 수정
☐ 3. 단위 테스트 작성 및 실행
☐ 4. 로컬 Docker Compose 테스트
☐ 5. Git commit (규칙 준수)
☐ 6. Pull Request 생성
☐ 7. 코드 리뷰 받기
☐ 8. PR merge
☐ 9. CI/CD 파이프라인 확인
☐ 10. K8s 배포 (kubectl rollout restart)
☐ 11. 배포 검증 (로그, API 테스트)
```

---

## 8. 트러블슈팅

### 8.1 Docker Compose 관련

#### 문제 1: 서비스가 시작되지 않음

**증상**:
```bash
$ docker-compose up member-service
ERROR: Service 'member-service' failed to build
```

**원인**: Dockerfile 경로 또는 빌드 컨텍스트 문제

**해결**:
```bash
# 1. Dockerfile 경로 확인
ls member-service/Dockerfile

# 2. 빌드 컨텍스트 확인
# docker-compose.yml에서 context: . (프로젝트 루트)

# 3. 캐시 제거 후 재빌드
docker-compose build --no-cache member-service

# 4. 재시작
docker-compose up member-service
```

#### 문제 2: MySQL 연결 실패

**증상**:
```
Communications link failure
```

**원인**: MySQL 컨테이너가 준비되기 전에 서비스 시작

**해결**:
```yaml
# docker-compose.yml 확인
member-service:
  depends_on:
    mysql-member:
      condition: service_healthy  # healthcheck 통과 대기
```

**MySQL healthcheck 확인**:
```bash
# MySQL 상태 확인
docker-compose ps mysql-member

# 로그 확인
docker-compose logs mysql-member

# 수동 헬스체크
docker exec mysql-member mysqladmin ping -h localhost -u root -prootpass
```

#### 문제 3: Eureka에 서비스 등록 안 됨

**증상**:
- Eureka Dashboard에 서비스가 보이지 않음

**원인**:
1. Eureka Server가 시작되지 않음
2. 환경변수 `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` 미설정

**해결**:
```bash
# 1. Eureka Server 상태 확인
curl http://localhost:8761/actuator/health

# 2. 서비스의 Eureka 설정 확인
docker exec member-service env | grep EUREKA

# 3. docker-compose.yml 확인
member-service:
  environment:
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/

# 4. 서비스 재시작
docker-compose restart member-service

# 5. 로그 확인
docker-compose logs member-service | grep -i eureka
```

#### 문제 4: 포트 충돌

**증상**:
```
Error starting userland proxy: listen tcp4 0.0.0.0:8004: bind: address already in use
```

**해결**:
```bash
# 1. 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8004

# 2. 프로세스 종료 (Windows)
taskkill /PID <PID> /F

# 3. 또는 docker-compose.yml에서 포트 변경
member-service:
  ports:
    - "8014:8004"  # 호스트 포트 변경
```

### 8.2 Kubernetes 관련

#### 문제 1: Pod가 Pending 상태

**증상**:
```bash
$ kubectl get pods -n tori-app
NAME                               READY   STATUS    RESTARTS   AGE
member-service-xxx                 0/1     Pending   0          5m
```

**원인**: 리소스 부족 또는 노드 스케줄링 실패

**해결**:
```bash
# 1. Pod 상세 정보 확인
kubectl describe pod member-service-xxx -n tori-app

# 2. 이벤트 확인
kubectl get events -n tori-app --sort-by='.lastTimestamp'

# 3. 노드 리소스 확인
kubectl top nodes

# 4. 리소스 제한 조정 (필요 시)
# k8s/services/member-service.yaml
resources:
  requests:
    memory: "128Mi"  # 줄임
    cpu: "100m"
```

#### 문제 2: Pod가 CrashLoopBackOff

**증상**:
```bash
$ kubectl get pods -n tori-app
NAME                               READY   STATUS             RESTARTS   AGE
member-service-xxx                 0/1     CrashLoopBackOff   5          10m
```

**원인**: 애플리케이션 시작 실패

**해결**:
```bash
# 1. 로그 확인
kubectl logs member-service-xxx -n tori-app

# 2. 이전 컨테이너 로그 확인 (재시작 후)
kubectl logs member-service-xxx -n tori-app --previous

# 3. 일반적 원인
# - DB 연결 실패 → ConfigMap/Secrets 확인
# - 환경변수 누락 → Deployment 확인
# - 포트 충돌 → application.properties 확인

# 4. ConfigMap 확인
kubectl get configmap app-config -n tori-app -o yaml

# 5. Secrets 확인
kubectl get secret app-secret -n tori-app -o yaml
```

#### 문제 3: Service 연결 실패

**증상**:
- frontend-service에서 member-service 호출 시 타임아웃

**해결**:
```bash
# 1. Service 존재 확인
kubectl get svc -n tori-app

# 2. Endpoints 확인 (Pod가 연결되었는지)
kubectl get endpoints member-service -n tori-app

# 3. Pod에서 직접 테스트
kubectl exec -it <frontend-pod> -n tori-app -- sh
curl http://member-service:8004/actuator/health

# 4. DNS 확인
kubectl exec -it <frontend-pod> -n tori-app -- nslookup member-service

# 5. NetworkPolicy 확인 (있는 경우)
kubectl get networkpolicy -n tori-app
```

#### 문제 4: 이미지 Pull 실패

**증상**:
```
Failed to pull image "490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/member-service:latest": rpc error: code = Unknown desc = Error response from daemon: pull access denied
```

**해결**:
```bash
# 1. ECR 인증 확인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com

# 2. 이미지 존재 확인
aws ecr describe-images --repository-name member-service --region ap-northeast-2

# 3. K8s Secret 확인 (ECR 인증용)
kubectl get secret -n tori-app

# 4. Secret 재생성 (필요 시)
kubectl create secret docker-registry ecr-secret \
  --docker-server=490866675691.dkr.ecr.ap-northeast-2.amazonaws.com \
  --docker-username=AWS \
  --docker-password=$(aws ecr get-login-password --region ap-northeast-2) \
  -n tori-app

# 5. Deployment에 imagePullSecrets 추가
spec:
  template:
    spec:
      imagePullSecrets:
      - name: ecr-secret
```

### 8.3 RabbitMQ 관련

#### 문제 1: 메시지가 전달되지 않음

**증상**:
- order-service에서 메시지 발행하지만 inventory-service에서 수신 안 됨

**해결**:
```bash
# 1. RabbitMQ Management UI 접속
# http://localhost:15672 (guest/guest)

# 2. Exchange 확인
# order.exchange가 존재하는지 확인

# 3. Queue 확인
# inventory.order.queue가 존재하는지 확인
# Ready 메시지 수 확인

# 4. Binding 확인
# order.exchange → inventory.order.queue
# Routing Key: order.placed

# 5. Consumer 연결 확인
# inventory.order.queue → Consumers 탭
# 연결된 Consumer가 있는지 확인

# 6. order-service 로그 확인
docker-compose logs order-service | grep -i rabbit

# 7. inventory-service 로그 확인
docker-compose logs inventory-service | grep -i rabbit

# 8. RabbitConfig 비교
# order-service: ORDER_EXCHANGE = "order.exchange"
# inventory-service: ORDER_EXCHANGE = "order.exchange"
# (이름이 일치해야 함)
```

#### 문제 2: DLQ (Dead Letter Queue)로 메시지 이동

**증상**:
- 메시지 처리 실패 후 재시도 없이 사라짐

**해결**:
```java
// RabbitConfig에 DLQ 설정 추가
@Bean
public Queue inventoryOrderQueue() {
    return QueueBuilder.durable(INVENTORY_ORDER_QUEUE)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "dlq.inventory.order")
            .build();
}

@Bean
public Queue deadLetterQueue() {
    return new Queue("dlq.inventory.order", true);
}

@Bean
public DirectExchange deadLetterExchange() {
    return new DirectExchange("dlx.exchange");
}

@Bean
public Binding dlqBinding() {
    return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dlq.inventory.order");
}
```

### 8.4 빌드 관련

#### 문제 1: Gradle 빌드 실패

**증상**:
```
Could not resolve all dependencies for configuration ':member-service:compileClasspath'
```

**해결**:
```bash
# 1. Gradle 캐시 삭제
./gradlew clean

# 2. 의존성 다시 다운로드
./gradlew build --refresh-dependencies

# 3. Gradle Wrapper 업데이트
./gradlew wrapper --gradle-version 8.5
```

#### 문제 2: 테스트 실패

**증상**:
```
OrderServiceTest > placeOrder_Success() FAILED
```

**해결**:
```bash
# 1. 특정 테스트만 실행
./gradlew :order-service:test --tests OrderServiceTest.placeOrder_Success

# 2. 테스트 리포트 확인
# order-service/build/reports/tests/test/index.html

# 3. 로그 출력
./gradlew :order-service:test --info

# 4. 테스트 무시하고 빌드 (임시)
./gradlew :order-service:build -x test
```

### 8.5 트러블슈팅 체크리스트

```
☐ 1. 로그 확인 (docker-compose logs / kubectl logs)
☐ 2. 헬스체크 확인 (/actuator/health)
☐ 3. 환경변수 확인 (env | grep XXX)
☐ 4. 네트워크 연결 확인 (ping, curl, nslookup)
☐ 5. 리소스 사용량 확인 (docker stats / kubectl top)
☐ 6. 설정 파일 비교 (로컬 vs 배포)
☐ 7. 의존 서비스 상태 확인 (DB, Eureka, RabbitMQ)
☐ 8. 이벤트 확인 (kubectl get events)
```

---

## 9. 서비스 포트 맵

### 9.1 전체 포트 맵

| 서비스명 | 컨테이너 포트 | 호스트 포트 | 용도 |
|---------|-------------|------------|------|
| **Infrastructure** |
| mysql-member | 3306 | 3306 | Member/Admin/Board DB |
| mysql-product | 3306 | 3307 | Product DB |
| mysql-order | 3306 | 3308 | Order DB |
| mysql-inventory | 3306 | 3309 | Inventory DB |
| rabbitmq | 5672 | 5672 | AMQP 프로토콜 |
| rabbitmq | 15672 | 15672 | Management UI |
| **MSA Services** |
| eureka-server | 8761 | 8761 | Service Discovery |
| gateway-service | 8000 | 8000 | API Gateway |
| product-service | 8001 | 8001 | 상품 관리 |
| order-service | 8002 | 8002 | 주문 관리 |
| member-service | 8004 | 8004 | 회원 관리 |
| frontend-service | 8005 | 8005 | 프론트엔드 (Thymeleaf) |
| board-service | 8006 | 8006 | 게시판/댓글 |
| admin-service | 8007 | 8007 | 관리자 기능 |
| inventory-service | 8008 | 8008 | 재고 관리 |

### 9.2 포트 구성 규칙

#### Docker Compose 환경

- **MySQL**: 3306(member), 3307(product), 3308(order), 3309(inventory)
- **RabbitMQ**: 5672 (AMQP), 15672 (Management)
- **서비스**: 각 서비스마다 고유 포트 (800X)
- **Gateway**: 8000 (모든 외부 요청 진입점)

#### Kubernetes 환경

- **ClusterIP**: 서비스 간 내부 통신만 허용
- **Ingress**: 외부 트래픽은 ALB → Gateway로 진입
- **포트**: 컨테이너 포트만 사용 (호스트 포트 없음)

### 9.3 접근 URL

#### 로컬 개발 (Docker Compose)

```bash
# Eureka Dashboard
http://localhost:8761

# API Gateway
http://localhost:8000

# RabbitMQ Management
http://localhost:15672 (guest/guest)

# 개별 서비스 (Gateway 없이 직접 접근)
http://localhost:8001  # product-service
http://localhost:8002  # order-service
http://localhost:8004  # member-service
http://localhost:8005  # frontend-service
```

#### 프로덕션 (Kubernetes)

```bash
# Ingress (ALB)
http://<ALB-DNS-NAME>

# Gateway (Internal)
http://gateway-service:8000

# 개별 서비스 (Internal)
http://member-service:8004
http://product-service:8001
```

---

## 10. 서비스 간 의존성 맵

### 10.1 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                         외부 클라이언트                        │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP
                        ▼
              ┌──────────────────┐
              │  Gateway Service │  :8000
              │  (Spring Cloud)  │
              └────────┬─────────┘
                       │
         ┌─────────────┼────────────┬──────────────┐
         │             │            │              │
         ▼             ▼            ▼              ▼
  ┌──────────┐  ┌──────────┐ ┌──────────┐  ┌──────────┐
  │ Frontend │  │  Member  │ │ Product  │  │  Order   │
  │ Service  │  │ Service  │ │ Service  │  │ Service  │
  │  :8005   │  │  :8004   │ │  :8001   │  │  :8002   │
  └─────┬────┘  └─────┬────┘ └─────┬────┘  └─────┬────┘
        │             │            │              │
        │             ▼            ▼              │
        │      ┌──────────┐ ┌──────────┐         │
        │      │  MySQL   │ │  MySQL   │         │
        │      │ Member   │ │ Product  │         │
        │      │  :3306   │ │  :3307   │         │
        │      └──────────┘ └──────────┘         │
        │                                         │
        │                                    RabbitMQ
        │                                         │
        │                                         ▼
        │                                  ┌──────────┐
        │                                  │Inventory │
        │                                  │ Service  │
        │                                  │  :8008   │
        │                                  └─────┬────┘
        │                                        │
        │                                        ▼
        │                                  ┌──────────┐
        │                                  │  MySQL   │
        │                                  │Inventory │
        │                                  │  :3309   │
        │                                  └──────────┘
        │
        ├────────────┬──────────────┐
        │            │              │
        ▼            ▼              ▼
  ┌──────────┐ ┌──────────┐ ┌──────────┐
  │  Board   │ │  Admin   │ │  Member  │
  │ Service  │ │ Service  │ │ Service  │
  │  :8006   │ │  :8007   │ │  :8004   │
  └─────┬────┘ └─────┬────┘ └──────────┘
        │            │
        └────────────┼─────────────┐
                     ▼             │
              ┌──────────┐         │
              │  MySQL   │◄────────┘
              │ Member   │
              │  :3306   │  (공유 DB)
              └──────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Eureka Server :8761                       │
│            (모든 서비스가 등록 및 디스커버리)                   │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 의존성 상세

#### Gateway Service → 모든 서비스

**의존 관계**:
```
gateway-service
  → eureka-server (서비스 디스커버리)
  → member-service (라우팅)
  → product-service (라우팅)
  → order-service (라우팅)
  → inventory-service (라우팅)
  → board-service (라우팅)
  → admin-service (라우팅)
  → frontend-service (라우팅)
```

**설정 파일**: `gateway-service/src/main/resources/application.properties`
```properties
spring.cloud.gateway.routes[0].id=member-service
spring.cloud.gateway.routes[0].uri=lb://member-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**,/api/auth/**

spring.cloud.gateway.routes[1].id=product-service
spring.cloud.gateway.routes[1].uri=lb://product-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/menus/**,/api/options/**

# ... (총 17개 라우트)
```

#### Frontend Service → Backend Services

**의존 관계**:
```
frontend-service
  → member-service (REST API)
  → product-service (REST API)
  → order-service (REST API)
  → board-service (REST API)
  → admin-service (REST API)
  → inventory-service (REST API)
```

**환경변수**: `docker-compose.yml`
```yaml
frontend-service:
  environment:
    SERVICE_MEMBER_URL: http://member-service:8004
    SERVICE_BOARD_URL: http://board-service:8006
    SERVICE_ADMIN_URL: http://admin-service:8007
    SERVICE_PRODUCT_URL: http://product-service:8001
    SERVICE_ORDER_URL: http://order-service:8002
    SERVICE_INVENTORY_URL: http://inventory-service:8008
```

#### Order Service → Inventory Service

**의존 관계**:
```
order-service
  → rabbitmq (메시지 발행)
  → inventory-service (비동기 이벤트)
```

**메시지 플로우**:
```
1. 주문 생성 (order-service)
2. RabbitMQ에 "order.placed" 이벤트 발행
3. inventory-service가 메시지 수신
4. 재고 차감
```

**Exchange/Queue/Binding**:
- **Exchange**: `order.exchange` (TopicExchange)
- **Routing Key**: `order.placed`
- **Queue**: `inventory.order.queue`
- **Binding**: `order.exchange` → `inventory.order.queue` (with `order.placed`)

#### Member/Admin/Board Service → MySQL Member

**의존 관계**:
```
member-service  ┐
admin-service   ├→ mysql-member:3306/member_db
board-service   ┘
```

**공유 테이블**:
- `users` (회원 정보)
- `boards` (게시판)
- `comments` (댓글)
- `inquiries` (문의)
- `notices` (공지사항)

**주의**: 동일 DB를 공유하므로 스키마 변경 시 3개 서비스 모두 영향받음

#### 모든 서비스 → Eureka Server

**의존 관계**:
```
모든 MSA 서비스
  → eureka-server:8761
```

**등록 정보**:
- 서비스명 (`spring.application.name`)
- IP 주소
- 포트
- 상태 (UP/DOWN)

**Heartbeat**: 30초마다

### 10.3 데이터 플로우 예시

#### 예시 1: 회원가입 플로우

```
1. 클라이언트 → Gateway :8000
   POST /api/auth/register

2. Gateway → Member Service :8004
   Load Balancing via Eureka

3. Member Service → MySQL Member :3306
   INSERT INTO users ...

4. Member Service → 클라이언트
   200 OK + { "message": "회원가입 성공" }
```

#### 예시 2: 주문 생성 플로우

```
1. 클라이언트 → Gateway :8000
   POST /api/orders

2. Gateway → Order Service :8002
   Load Balancing via Eureka

3. Order Service → MySQL Order :3308
   INSERT INTO orders ...
   INSERT INTO order_items ...

4. Order Service → RabbitMQ :5672
   Publish "OrderPlacedEvent" to "order.exchange"

5. RabbitMQ → Inventory Service :8008
   Deliver message to "inventory.order.queue"

6. Inventory Service → MySQL Inventory :3309
   UPDATE material_master SET stock = stock - quantity

7. Order Service → 클라이언트
   200 OK + { "orderId": 123 }
```

### 10.4 Breaking Changes (의존성 변경 시 주의)

#### ⚠️ RabbitMQ Exchange/Routing Key 변경

**변경 대상**:
- `order-service/RabbitConfig.ORDER_EXCHANGE`
- `inventory-service/RabbitConfig.ORDER_EXCHANGE`

**영향**:
- 두 서비스의 Exchange 이름이 일치하지 않으면 메시지 전달 실패

**해결**:
```java
// order-service
public static final String ORDER_EXCHANGE = "order.events";  // 변경

// inventory-service (동일하게 변경 필수!)
public static final String ORDER_EXCHANGE = "order.events";  // 변경
```

#### ⚠️ Gateway 라우팅 Path 변경

**변경 대상**:
- `gateway-service/application.properties`

**영향**:
- Path 패턴이 중복되면 먼저 매칭되는 라우트가 우선

**문제 예시**:
```properties
# ❌ 잘못된 예
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/**  # 너무 포괄적
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/users/**  # 위 라우트에 먹힘
```

**해결**:
```properties
# ✅ 올바른 예
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**  # 구체적 먼저
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/products/**
```

#### ⚠️ 공유 DB 스키마 변경

**변경 대상**:
- `member-service/model/Member.java`

**영향**:
- member-service, admin-service, board-service 모두 영향

**해결**:
- 3개 서비스 모두 재배포 필요

### 10.5 의존성 체크리스트

```
☐ 1. Eureka Server 먼저 시작
☐ 2. MySQL 컨테이너 healthcheck 통과 대기
☐ 3. RabbitMQ healthcheck 통과 대기
☐ 4. Gateway Service 시작 (모든 라우트 설정 완료)
☐ 5. Backend Services 시작 (Eureka 등록 확인)
☐ 6. Frontend Service 시작 (환경변수 확인)
☐ 7. RabbitMQ Binding 확인 (Exchange → Queue)
☐ 8. 서비스 간 통신 테스트 (curl/Postman)
```

---

## 부록 A: 빠른 참조

### A.1 자주 사용하는 명령어

#### Docker Compose

```bash
# 전체 실행
docker-compose up -d

# 특정 서비스만 실행
docker-compose up -d member-service

# 로그 확인
docker-compose logs -f member-service

# 재시작
docker-compose restart member-service

# 중지
docker-compose stop

# 삭제 (데이터 보존)
docker-compose down

# 삭제 (데이터 포함)
docker-compose down -v

# 재빌드
docker-compose build --no-cache member-service
```

#### Kubernetes

```bash
# Pod 목록
kubectl get pods -n tori-app

# Pod 로그
kubectl logs -f deployment/member-service -n tori-app

# 배포
kubectl rollout restart deployment/member-service -n tori-app

# 배포 상태
kubectl rollout status deployment/member-service -n tori-app

# 롤백
kubectl rollout undo deployment/member-service -n tori-app

# 리소스 사용량
kubectl top pods -n tori-app

# ConfigMap 수정
kubectl edit configmap app-config -n tori-app

# Secrets 수정
kubectl edit secret app-secret -n tori-app
```

#### Gradle

```bash
# 전체 빌드
./gradlew build

# 특정 서비스 빌드
./gradlew :member-service:build

# 테스트 제외 빌드
./gradlew build -x test

# 테스트만 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

### A.2 환경변수 Quick Reference

| 변수명 | 용도 | 기본값 |
|--------|------|-------|
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | - |
| `JWT_EXPIRATION` | Access Token 만료 시간 (ms) | 86400000 (1시간) |
| `SPRING_DATASOURCE_URL` | DB 연결 URL | jdbc:mysql://localhost:3306/... |
| `SPRING_RABBITMQ_HOST` | RabbitMQ 호스트 | localhost |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka URL | http://localhost:8761/eureka/ |
| `SERVICE_MEMBER_URL` | Member Service URL | http://member-service:8004 |

### A.3 트러블슈팅 Quick Reference

| 증상 | 원인 | 해결 |
|------|------|------|
| `Communications link failure` | MySQL 미시작 | `docker-compose up mysql-member` |
| `Service not found in Eureka` | Eureka 미등록 | 환경변수 `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` 확인 |
| `Port already in use` | 포트 충돌 | `netstat -ano \| findstr :8004` 후 프로세스 종료 |
| `CrashLoopBackOff` | Pod 시작 실패 | `kubectl logs <pod> -n tori-app` |
| `ImagePullBackOff` | ECR 인증 실패 | `aws ecr get-login-password` |
| RabbitMQ 메시지 미전달 | Binding 미설정 | RabbitMQ Management UI 확인 |

---

**문서 버전**: 1.0
**최종 수정일**: 2024-04-15
**작성자**: AI Documentation Generator