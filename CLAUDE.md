# CLAUDE.md - TORI Coffee MSA 프로젝트 AI 개발 가이드

> 이 문서는 AI 어시스턴트가 이 프로젝트를 이해하고 올바르게 코드를 수정하기 위한 필수 가이드입니다.
> **수정 전 반드시 이 문서를 읽고 규칙을 준수하세요.**

---

## 섹션 1: 프로젝트 개요

### 프로젝트 목적
TORI Coffee는 Spring Cloud 기반 마이크로서비스 아키텍처(MSA)로 구현한 커피숍 주문/재고 관리 시스템입니다.

### 핵심 기능
- **주문 관리**: 장바구니, 주문 생성, 주문 내역 조회
- **재고 관리**: 레시피 기반 자동 재고 차감 (비동기 처리)
- **회원 관리**: JWT 인증, OAuth2 소셜 로그인, 계정 잠금
- **상품 관리**: 메뉴, 옵션, 알레르기 정보, 영양정보
- **게시판**: 공지사항, 이벤트, 1:1 문의

### 시스템 아키텍처 요약
```
Client → AWS ALB → Gateway(8000) → [Eureka 기반 Service Discovery]
                                         ↓
                    ┌─────────────────────┼─────────────────────┐
                    ↓                     ↓                     ↓
             member-service         order-service        inventory-service
                (8004)                 (8002)                (8008)
                    ↓                     ↓                     ↓
              member_db              order_db            inventory_db
               (3306)                (3308)                (3309)
                                         ↓
                                    RabbitMQ ───→ inventory-service
                                  (비동기 재고 차감)
```

### 비동기 메시징 흐름
1. `order-service`가 주문 저장 후 RabbitMQ에 메시지 발행
2. Exchange: `order.exchange`, Routing Key: `order.placed`
3. `inventory-service`가 `inventory.order.queue`에서 메시지 수신
4. 레시피 기반으로 재료 재고 차감

---

## 섹션 2: 기술 스택 정확한 버전

| 카테고리 | 기술 | 버전 |
|---------|------|------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.1.5 |
| **Cloud** | Spring Cloud | 2022.0.4 |
| **Service Discovery** | Netflix Eureka | Spring Cloud 2022.0.4 |
| **API Gateway** | Spring Cloud Gateway | Reactive (WebFlux) |
| **Message Queue** | RabbitMQ | 3.12+ (Topic Exchange) |
| **Database** | MySQL | 8.0 |
| **ORM** | Hibernate/JPA | Spring Boot 3.1.5 내장 |
| **Authentication** | JWT | jjwt 0.11.5, HS512 알고리즘 |
| **Build** | Gradle | 8.x (Multi-module) |
| **Container** | Docker | Multi-stage build |
| **Orchestration** | Kubernetes | AWS EKS |
| **Template Engine** | Thymeleaf | (frontend-service) |

### 주요 의존성 (build.gradle)
```groovy
// 루트 build.gradle
springBootVersion = '3.1.5'
springCloudVersion = '2022.0.4'
sourceCompatibility = '17'

// 공통 의존성
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.projectlombok:lombok'
runtimeOnly 'com.mysql:mysql-connector-j'

// JWT (member-service, inventory-service)
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

// RabbitMQ (order-service, inventory-service, product-service)
implementation 'org.springframework.boot:spring-boot-starter-amqp'

// Prometheus 모니터링
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

---

## 섹션 3: 디렉토리 구조

```
teamprojectv1/
├── build.gradle                    # 루트 빌드 설정 (버전 정의)
├── docker-compose.yml              # 로컬 개발용 전체 인프라
├── .github/workflows/ci.yml        # GitHub Actions CI 파이프라인
│
├── eureka-server/                  # 서비스 레지스트리 (8761)
│   └── src/main/java/com/example/EurekaServerApplication.java
│
├── gateway-service/                # API Gateway (8000)
│   └── src/main/resources/application.properties  # 17개 라우팅 규칙
│
├── frontend-service/               # Thymeleaf 웹 UI (8005)
│   ├── src/main/java/com/toricoffee/frontend/controller/
│   └── src/main/resources/templates/
│
├── member-service/                 # 회원/인증 (8004)
│   ├── src/main/java/com/example/member/
│   │   ├── controller/AuthController.java      # 로그인, 회원가입
│   │   ├── service/MemberService.java
│   │   ├── model/Member.java                   # @Entity
│   │   ├── repository/MemberRepository.java
│   │   ├── util/JwtUtil.java                   # JWT 생성/검증
│   │   └── config/SecurityConfig.java
│   └── src/main/resources/application.properties
│
├── product-service/                # 상품 관리 (8001)
│   ├── src/main/java/com/example/product/
│   │   ├── controller/MenuController.java
│   │   ├── service/MenuService.java
│   │   ├── model/                  # Menu, Allergy, Nutrition, Recipe, OptionMaster
│   │   └── repository/
│   └── src/main/resources/application.properties
│
├── order-service/                  # 주문/장바구니 (8002) - RabbitMQ Producer
│   ├── src/main/java/com/example/cust/
│   │   ├── controller/OrderController.java
│   │   ├── service/OrderService.java           # placeOrder() → RabbitMQ 발행
│   │   ├── model/                  # Orders, OrderItem, OrderOption, CartHeader, CartItem
│   │   ├── dto/OrderStockMessage.java          # RabbitMQ 메시지 DTO
│   │   ├── config/RabbitConfig.java            # Exchange 정의
│   │   └── repository/
│   └── src/test/java/.../OrderServiceTest.java # 단위 테스트
│
├── inventory-service/              # 재고 관리 (8008) - RabbitMQ Consumer
│   ├── src/main/java/com/example/inventory/
│   │   ├── controller/InventoryController.java
│   │   ├── service/InventoryServiceImpl.java   # processOrderStock()
│   │   ├── messaging/OrderMessageListener.java # @RabbitListener
│   │   ├── model/                  # MaterialMaster, Recipe, OptionMaster
│   │   ├── dto/OrderStockRequestDto.java
│   │   └── config/RabbitConfig.java            # Queue, Binding 정의
│   └── src/main/resources/application.properties
│
├── board-service/                  # 게시판 (8006)
│   └── src/main/java/com/example/boardservice/
│
├── admin-service/                  # 관리자 (8007)
│   └── src/main/java/com/du/adminservice/
│
├── k8s/                            # Kubernetes 매니페스트
│   ├── base/
│   │   ├── configmap.yaml          # 서비스 URL, Eureka, RabbitMQ 호스트
│   │   ├── secrets.yaml            # DB 비밀번호, JWT Secret
│   │   └── namespace.yaml          # tori-app 네임스페이스
│   ├── services/                   # 각 서비스 Deployment + Service
│   ├── mysql/                      # DB StatefulSet
│   ├── rabbitmq/
│   ├── hpa/hpa.yaml               # HorizontalPodAutoscaler
│   └── ingress.yaml
│
└── argocd/                         # ArgoCD GitOps
    ├── root-app.yaml
    └── apps/
```

---

## 섹션 4: 서비스별 개발 규칙

### 4.1 member-service (인증 서비스)

**패키지 구조**: `com.example.member`

**JWT 토큰 규칙**:
```java
// JwtUtil.java:34-45 참고
// Access Token: 1시간 만료, claim에 "role", "type":"access" 포함
// Refresh Token: 7일 만료, claim에 "type":"refresh" 포함
// 알고리즘: HS512
// Secret Key: 환경변수 JWT_SECRET (최소 32자)

String accessToken = jwtUtil.generateAccessToken(userId, userType);
String refreshToken = jwtUtil.generateRefreshToken(userId);
```

**계정 잠금 규칙** (Member.java:65-74):
- 로그인 5회 실패 시 30분 계정 잠금
- `member.recordLoginFailure()` 호출로 실패 기록
- `member.isAccountLocked()` 로 잠금 확인

**비밀번호 규칙** (AuthController.java:935-950):
- 8~20자, 영문/숫자/특수문자 중 2가지 이상 조합
- BCryptPasswordEncoder 사용

**API 응답 형식**:
```java
// 성공 응답
Map<String, Object> response = new HashMap<>();
response.put("success", true);
response.put("message", "처리 완료 메시지");
response.put("data", responseData);  // 선택적

// 에러 응답
Map<String, Object> error = new HashMap<>();
error.put("success", false);
error.put("message", "에러 메시지");
error.put("errorCode", "ERROR_CODE");
```

### 4.2 order-service (주문 서비스)

**패키지 구조**: `com.example.cust`

**RabbitMQ Producer 규칙** (RabbitConfig.java):
```java
public static final String ORDER_EXCHANGE = "order.exchange";
public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

// 메시지 발행 (OrderService.java:92-110)
rabbitTemplate.convertAndSend(
    RabbitConfig.ORDER_EXCHANGE,
    RabbitConfig.ORDER_PLACED_ROUTING_KEY,
    OrderStockMessage.from(order)
);
```

**주문 상태 Enum** (OrderStatus.java):
- `PENDING`: 주문 접수 (재고 확인 중)
- `PAYMENT_COMPLETED`: 결제 완료
- `CONFIRMED`: 주문 확정
- `CANCELLED`: 주문 취소

**비동기 응답 규칙** (OrderController.java:124-155):
```java
// 주문 생성 시 202 Accepted 반환 (비동기 처리)
return ResponseEntity.status(HttpStatus.ACCEPTED)
    .body(Map.of(
        "message", "주문이 접수되었습니다. 재고 확인 후 최종 확정됩니다.",
        "orderId", savedOrder.getOrderId(),
        "status", "PENDING"
    ));
```

**N+1 문제 방지** (OrdersRepository.java:30-35):
```java
@Query("SELECT o FROM Orders o " +
       "JOIN FETCH o.orderItems oi " +
       "LEFT JOIN FETCH oi.orderOptions oo " +
       "WHERE o.orderId = :orderId")
Optional<Orders> findDetailByIdWithItemsAndOptions(@Param("orderId") Integer orderId);
```

### 4.3 inventory-service (재고 서비스)

**패키지 구조**: `com.example.inventory`

**RabbitMQ Consumer 규칙** (RabbitConfig.java, OrderMessageListener.java):
```java
public static final String INVENTORY_ORDER_QUEUE = "inventory.order.queue";

// Queue와 Exchange 바인딩
@Bean
public Binding bindingInventoryQueue(Queue inventoryOrderQueue, TopicExchange orderExchange) {
    return BindingBuilder.bind(inventoryOrderQueue).to(orderExchange).with(ORDER_PLACED_ROUTING_KEY);
}

// 메시지 수신
@RabbitListener(queues = "inventory.order.queue")
public void handleOrderPlaced(OrderStockRequestDto request) {
    inventoryService.processOrderStock(request);
}
```

**재고 차감 로직** (InventoryServiceImpl.java):
- 레시피(`Recipe`)에서 메뉴별 기본 재료 수량 조회
- 옵션(`OptionMaster`)에서 추가/제거/변경 재료 계산
- `processMethod`: "추가", "제거", "변경" 처리
- 재고 부족 시 실패 응답 반환 (주문 롤백은 별도 처리 필요)

### 4.4 product-service (상품 서비스)

**패키지 구조**: `com.example.product`

**SQL 초기화** (application.properties):
```properties
spring.sql.init.mode=always
spring.sql.init.encoding=UTF-8
spring.jpa.defer-datasource-initialization=true
```

### 4.5 gateway-service (API Gateway)

**라우팅 규칙** (application.properties):
- 라우팅 순서가 중요함. 구체적인 경로가 먼저, `/**`는 맨 마지막
- `lb://` 접두사로 Eureka 기반 로드밸런싱
- `frontend-service` 라우트는 반드시 마지막에 위치 (catch-all)

```properties
# 순서 중요: 구체적인 경로 먼저
spring.cloud.gateway.routes[13].id=admin-product-service
spring.cloud.gateway.routes[13].uri=lb://product-service
spring.cloud.gateway.routes[13].predicates[0]=Path=/api/admin/products/**,...

spring.cloud.gateway.routes[14].id=admin-service
spring.cloud.gateway.routes[14].uri=lb://admin-service
spring.cloud.gateway.routes[14].predicates[0]=Path=/api/admin/**

# 맨 마지막: catch-all
spring.cloud.gateway.routes[16].id=frontend-service
spring.cloud.gateway.routes[16].predicates[0]=Path=/**
```

### 4.6 frontend-service (프론트엔드)

**패키지 구조**: `com.toricoffee.frontend`

**서비스 URL 환경변수**:
```properties
SERVICE_MEMBER_URL=http://member-service:8004
SERVICE_PRODUCT_URL=http://product-service:8001
SERVICE_ORDER_URL=http://order-service:8002
SERVICE_INVENTORY_URL=http://inventory-service:8008
```

---

## 섹션 5: 서비스 간 의존관계

### 데이터베이스 의존성

| 서비스 | 데이터베이스 | 포트 | 공유 여부 |
|--------|------------|------|----------|
| member-service | member_db | 3306 | - |
| board-service | member_db | 3306 | member-service와 공유 |
| admin-service | member_db | 3306 | member-service와 공유 |
| product-service | product_db | 3307 | 단독 |
| order-service | order_db | 3308 | 단독 |
| inventory-service | inventory_db | 3309 | 단독 |

### 메시지 의존성 (RabbitMQ)

```
order-service ──[order.exchange/order.placed]──> inventory-service
    (Producer)                                      (Consumer)
```

**변경 시 영향**:
- `OrderStockMessage` DTO 변경 시 → `OrderStockRequestDto`도 동기화 필요
- Exchange/Queue 이름 변경 시 → 양쪽 `RabbitConfig.java` 모두 수정

### 런타임 의존성

| 서비스 | 의존하는 서비스 | 의존 방식 |
|--------|---------------|----------|
| 모든 서비스 | eureka-server | 서비스 등록/발견 |
| gateway-service | 모든 백엔드 서비스 | HTTP 라우팅 |
| order-service | inventory-service | RabbitMQ 비동기 |
| frontend-service | 모든 백엔드 서비스 | HTTP 호출 (RestTemplate) |

### 바꾸면 깨지는 것

| 변경 대상 | 영향 받는 곳 | 증상 |
|----------|------------|------|
| `order.exchange` 이름 | inventory-service | 메시지 수신 불가, 재고 차감 안됨 |
| `OrderStockMessage` 필드 추가/삭제 | inventory-service `OrderStockRequestDto` | JSON 역직렬화 실패 |
| member_db 스키마 (users 테이블) | board-service, admin-service | JPA 매핑 에러 |
| JWT Secret 변경 | 모든 JWT 검증 서비스 | 토큰 검증 실패, 401 에러 |
| Eureka URL 변경 | 모든 서비스 | 서비스 등록 실패, 라우팅 불가 |
| Gateway 라우팅 순서 | 해당 API 호출 | 잘못된 서비스로 라우팅 |

---

## 섹션 6: 절대 하면 안 되는 것 (금지 패턴)

### 6.1 RabbitMQ 관련 금지

```java
// 금지: 동기적 재고 확인 후 주문 저장
public Orders placeOrder(...) {
    InventoryResult result = inventoryService.checkStock(order); // 동기 호출
    if (result.isSuccess()) {
        ordersRepository.save(order);
    }
}
// 이유: 서비스 간 결합도 증가, inventory-service 장애 시 주문 불가

// 올바른 방법: 비동기 메시지 발행
public Orders placeOrder(...) {
    Orders saved = ordersRepository.save(order);
    rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message); // 비동기
    return saved;
}
```

```java
// 금지: Queue 이름 하드코딩 분산
@RabbitListener(queues = "inventory.order.queue") // OrderMessageListener.java
private static final String QUEUE = "order.queue"; // 다른 파일에서 다른 이름

// 올바른 방법: RabbitConfig 상수 사용
@RabbitListener(queues = RabbitConfig.INVENTORY_ORDER_QUEUE)
```

### 6.2 JWT 관련 금지

```java
// 금지: JWT Secret 하드코딩
private String secret = "my-secret-key";

// 올바른 방법: 환경변수 사용
@Value("${jwt.secret}")
private String secret;
```

```java
// 금지: 토큰 타입 검증 누락
public boolean validateToken(String token) {
    Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    return true;
}

// 올바른 방법: 토큰 타입("access" vs "refresh") 검증 포함
public boolean validateAccessToken(String token) {
    Claims claims = ...;
    String type = claims.get("type", String.class);
    return "access".equals(type);  // 타입 확인 필수
}
```

### 6.3 Gateway 라우팅 금지

```properties
# 금지: frontend-service를 중간에 배치
spring.cloud.gateway.routes[5].id=frontend-service
spring.cloud.gateway.routes[5].predicates[0]=Path=/**
spring.cloud.gateway.routes[6].id=admin-service  # 이 라우트는 도달 불가!

# 올바른 방법: frontend-service(/**) 는 반드시 마지막
spring.cloud.gateway.routes[16].id=frontend-service
spring.cloud.gateway.routes[16].predicates[0]=Path=/**
```

### 6.4 데이터베이스 관련 금지

```java
// 금지: member_db에 order-service에서 직접 접근
// order-service의 application.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/member_db  // 금지!

// 이유: 서비스별 DB 독립성 위반
```

```java
// 금지: JPA 연관관계 없이 N+1 쿼리 발생
List<Orders> orders = ordersRepository.findAll();
for (Orders order : orders) {
    order.getOrderItems().forEach(...);  // 각 주문마다 쿼리 발생
}

// 올바른 방법: Fetch Join 사용
@Query("SELECT o FROM Orders o JOIN FETCH o.orderItems")
List<Orders> findAllWithItems();
```

### 6.5 환경변수 관련 금지

```yaml
# 금지: 비밀번호 평문 커밋
# k8s/base/secrets.yaml
stringData:
  MYSQL_MEMBER_PASSWORD: "actual-password-here"

# 올바른 방법: 플레이스홀더 사용, 실제 값은 CI/CD에서 주입
stringData:
  MYSQL_MEMBER_PASSWORD: "your-password-here"
```

### 6.6 테스트 관련 금지

```java
// 금지: 실제 DB/RabbitMQ 연결하는 단위 테스트
@SpringBootTest  // 전체 컨텍스트 로드
class OrderServiceTest {
    @Autowired OrderService orderService;  // 실제 빈 주입
}

// 올바른 방법: Mock 사용
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrdersRepository ordersRepository;
    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks OrderService orderService;
}
```

---

## 섹션 7: 네이밍 컨벤션

### 7.1 패키지명

| 서비스 | 패키지 | 비고 |
|--------|--------|------|
| member-service | `com.example.member` | |
| order-service | `com.example.cust` | 주의: cust (customer 약자) |
| inventory-service | `com.example.inventory` | |
| product-service | `com.example.product` | |
| board-service | `com.example.boardservice` | |
| admin-service | `com.du.adminservice` | 주의: 다른 그룹명 |
| frontend-service | `com.toricoffee.frontend` | 주의: 다른 그룹명 |

### 7.2 클래스명

```
Controller: {도메인}Controller.java     예: OrderController, AuthController
Service:    {도메인}Service.java        예: OrderService, MemberService
Repository: {엔티티}Repository.java     예: OrdersRepository, MemberRepository
Entity:     단수형.java                 예: Member, Orders, Menu
DTO:        {용도}Dto.java              예: OrderDetailDto, OrderStockMessage
Config:     {기능}Config.java           예: RabbitConfig, SecurityConfig
```

### 7.3 테이블명 vs 엔티티명

| 엔티티 | 테이블명 | @Table 필수 여부 |
|--------|---------|-----------------|
| Member | users | 필수 (`@Table(name = "users")`) |
| Orders | orders | 필수 (SQL 예약어) |
| Menu | menu | |
| CartHeader | cart_header | |
| MaterialMaster | material_master | |

### 7.4 API 경로 규칙

```
/api/{서비스도메인}/{리소스}
/api/auth/login          # member-service
/api/order/cart          # order-service
/api/products/**         # product-service
/api/admin/**            # admin-service
/api/boards/**           # board-service
/api/inventory/**        # inventory-service
```

### 7.5 RabbitMQ 네이밍

```
Exchange: {도메인}.exchange       예: order.exchange
Queue:    {소비자}.{도메인}.queue  예: inventory.order.queue
Routing:  {도메인}.{액션}          예: order.placed
```

---

## 섹션 8: 테스트 작성 규칙

### 8.1 테스트 프레임워크

```java
// 단위 테스트 (Mock 사용)
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrdersRepository ordersRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private OrderService orderService;
}
```

### 8.2 테스트 구조 (Given-When-Then)

```java
// OrderServiceTest.java:93-130 참고
@Test
@DisplayName("주문 생성 성공 - 장바구니 아이템이 주문으로 변환되어야 한다")
void placeOrder_Success() {
    // given
    String customerId = "testUser";
    when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(testCartHeader);
    when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
        Orders order = invocation.getArgument(0);
        order.setOrderId(1);
        return order;
    });

    // when
    Orders result = orderService.placeOrder(customerId, "테스트유저", "요청사항");

    // then
    assertThat(result).isNotNull();
    assertThat(result.getCustomerId()).isEqualTo(customerId);
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

    // RabbitMQ 메시지 전송 확인
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitConfig.ORDER_EXCHANGE),
        eq(RabbitConfig.ORDER_PLACED_ROUTING_KEY),
        any()
    );
}
```

### 8.3 예외 테스트

```java
@Test
@DisplayName("주문 생성 실패 - 빈 장바구니일 경우 예외 발생")
void placeOrder_EmptyCart_ThrowsException() {
    // given
    when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(emptyCartHeader);

    // when & then
    assertThatThrownBy(() -> orderService.placeOrder(customerId, "테스트", "요청사항"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("장바구니가 비어있습니다.");

    // 주문이 저장되지 않았는지 확인
    verify(ordersRepository, never()).save(any());
}
```

### 8.4 비동기 처리 테스트

```java
@Test
@DisplayName("RabbitMQ 메시지 전송 실패해도 주문은 저장되어야 한다")
void placeOrder_RabbitMQFails_OrderStillSaved() {
    // RabbitMQ 전송 시 예외 발생
    doThrow(new RuntimeException("RabbitMQ connection failed"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any());

    // when
    Orders result = orderService.placeOrder(customerId, "테스트유저", "요청사항");

    // then - 주문은 여전히 저장됨
    assertThat(result).isNotNull();
    verify(ordersRepository).save(any(Orders.class));
}
```

### 8.5 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 서비스 테스트
./gradlew :order-service:test

# 특정 테스트 클래스
./gradlew :order-service:test --tests "OrderServiceTest"
```

---

## 섹션 9: Git 커밋 규칙

### 9.1 브랜치 전략

```
main              # 프로덕션 브랜치 (CI/CD 자동 배포)
develop           # 개발 통합 브랜치
feature/{기능명}   # 기능 개발
fix/{버그내용}     # 버그 수정
experiment/{실험} # 실험적 기능
```

### 9.2 커밋 메시지 형식

```
{타입}: {간단한 설명}

{상세 내용 (선택)}

{이슈 참조 (선택)}
```

### 9.3 커밋 타입

| 타입 | 설명 | 예시 |
|------|------|------|
| feat | 새 기능 추가 | feat: add order history API |
| fix | 버그 수정 | fix: resolve N+1 query in order detail |
| refactor | 코드 리팩토링 | refactor: extract JWT validation logic |
| docs | 문서 수정 | docs: update API documentation |
| test | 테스트 추가/수정 | test: add OrderService unit tests |
| chore | 빌드/설정 변경 | chore: update gradle dependencies |
| ci | CI/CD 변경 | ci: add ECR push to workflow |

### 9.4 커밋 예시 (실제 프로젝트)

```bash
# 최근 커밋 참고
git log --oneline -5

fe5ab00 update project
7c2ac9d fix: remove tori-app prefix from all ECR image paths
40586ad feat: add ArgoCD root app
8de4d13 feat ci.yml add ecr push
bf1315c feat change ci.yml
```

---

## 섹션 10: 환경변수 목록

### 10.1 공통 환경변수

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka 서버 URL | `http://localhost:8761/eureka/` | O |
| `SPRING_DATASOURCE_URL` | DB 연결 URL | 서비스별 상이 | O |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 | 서비스별 상이 | O |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | - | O |

### 10.2 member-service 환경변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | `your-256-bit-secret...` |
| `JWT_EXPIRATION` | Access Token 만료시간(ms) | `86400000` (24시간) |
| `MAIL_USERNAME` | Gmail SMTP 사용자 | - |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 | - |

### 10.3 RabbitMQ 환경변수 (order-service, inventory-service)

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SPRING_RABBITMQ_HOST` | RabbitMQ 호스트 | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ 포트 | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ 사용자 | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ 비밀번호 | `guest` |

### 10.4 frontend-service 환경변수

| 변수명 | 설명 |
|--------|------|
| `SERVICE_MEMBER_URL` | `http://member-service:8004` |
| `SERVICE_BOARD_URL` | `http://board-service:8006` |
| `SERVICE_ADMIN_URL` | `http://admin-service:8007` |
| `SERVICE_PRODUCT_URL` | `http://product-service:8001` |
| `SERVICE_ORDER_URL` | `http://order-service:8002` |
| `SERVICE_INVENTORY_URL` | `http://inventory-service:8008` |

### 10.5 .env 파일 템플릿

```bash
# .env (Git에 커밋 금지)
# member-service용
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters-long
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# OAuth2 (선택)
GOOGLE_CLIENT_ID=xxx
GOOGLE_CLIENT_SECRET=xxx
NAVER_CLIENT_ID=xxx
NAVER_CLIENT_SECRET=xxx
```

---

## 섹션 11: 빌드/실행 명령어

### 11.1 로컬 개발 환경

```bash
# 1. 인프라 시작 (MySQL 4개 + RabbitMQ)
docker-compose up -d mysql-member mysql-product mysql-order mysql-inventory rabbitmq

# 2. 서비스 순차 실행 (순서 중요!)
./gradlew :eureka-server:bootRun      # 먼저 실행, 8761 대기
./gradlew :gateway-service:bootRun    # Eureka 등록 후
./gradlew :member-service:bootRun
./gradlew :product-service:bootRun
./gradlew :order-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :frontend-service:bootRun

# 또는 전체 Docker Compose
docker-compose up --build
```

### 11.2 개별 서비스 빌드

```bash
# 전체 빌드 (테스트 스킵)
./gradlew build -x test

# 특정 서비스 빌드
./gradlew :order-service:build -x test
./gradlew :member-service:build

# JAR 파일 위치
ls {서비스명}/build/libs/*.jar
```

### 11.3 Docker 이미지 빌드

```bash
# 루트 디렉토리에서 실행 (context가 루트)
docker build -f order-service/Dockerfile -t order-service:latest .
docker build -f member-service/Dockerfile -t member-service:latest .

# 모든 서비스 빌드 (docker-compose)
docker-compose build
```

### 11.4 Kubernetes 배포

```bash
# 네임스페이스 생성
kubectl apply -f k8s/base/namespace.yaml

# ConfigMap, Secrets 적용
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml

# MySQL StatefulSet
kubectl apply -f k8s/mysql/

# RabbitMQ
kubectl apply -f k8s/rabbitmq/

# 서비스 배포
kubectl apply -f k8s/services/

# HPA 적용
kubectl apply -f k8s/hpa/
```

### 11.5 접속 URL

| 서비스 | 로컬 URL |
|--------|----------|
| Gateway | http://localhost:8000 |
| Frontend | http://localhost:8005 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |

---

## 섹션 12: 자주 발생하는 문제 및 해결

### 12.1 Eureka 등록 실패

**증상**: 서비스 시작 시 `Connection refused` 에러
```
com.netflix.discovery.shared.transport.TransportException:
Cannot execute request on any known server
```

**원인**: eureka-server가 아직 시작되지 않음

**해결**:
1. eureka-server 먼저 시작 후 다른 서비스 시작
2. Docker Compose에서 `depends_on` + `healthcheck` 사용

### 12.2 RabbitMQ 연결 실패

**증상**: `AmqpConnectException: java.net.ConnectException`

**원인**: RabbitMQ 서버 미실행 또는 호스트명 오류

**해결**:
```bash
# RabbitMQ 상태 확인
docker-compose ps rabbitmq

# 환경변수 확인
echo $SPRING_RABBITMQ_HOST  # 로컬: localhost, Docker: rabbitmq
```

### 12.3 MySQL 연결 실패

**증상**: `Communications link failure`

**원인**: 포트 매핑 오류 또는 DB 미실행

**해결**:
```bash
# 포트 확인
# member_db: 3306
# product_db: 3307
# order_db: 3308
# inventory_db: 3309

docker-compose ps mysql-member
mysql -h localhost -P 3306 -u root -prootpass
```

### 12.4 JWT 검증 실패

**증상**: `401 Unauthorized`, `SignatureException`

**원인**: 서비스 간 JWT_SECRET 불일치

**해결**:
1. 모든 서비스에서 동일한 `JWT_SECRET` 환경변수 사용
2. Secret Key 길이 확인 (최소 32자)

### 12.5 Gateway 라우팅 오류

**증상**: 특정 API가 404 또는 잘못된 서비스로 라우팅

**원인**: 라우팅 순서 문제 또는 Path 패턴 충돌

**해결**:
```properties
# gateway-service/application.properties 확인
# 1. 구체적인 경로가 먼저 (예: /api/admin/products/**)
# 2. 일반적인 경로가 나중 (예: /api/admin/**)
# 3. catch-all은 맨 마지막 (예: /**)
```

### 12.6 N+1 쿼리 문제

**증상**: 주문 조회 시 쿼리가 대량 발생, 성능 저하

**원인**: Lazy Loading으로 연관 엔티티 개별 조회

**해결**:
```java
// Fetch Join 사용
@Query("SELECT o FROM Orders o " +
       "JOIN FETCH o.orderItems oi " +
       "LEFT JOIN FETCH oi.orderOptions " +
       "WHERE o.orderId = :orderId")
Optional<Orders> findDetailByIdWithItemsAndOptions(@Param("orderId") Integer orderId);
```

---

## 섹션 13: 코드리뷰 체크리스트

### API 변경 시
- [ ] Gateway 라우팅 규칙에 새 경로 추가했는가?
- [ ] 기존 API와 경로 충돌이 없는가?
- [ ] API 응답 형식이 `{success, message, data}` 또는 `{success, message, errorCode}` 패턴인가?

### 엔티티 변경 시
- [ ] 다른 서비스와 공유하는 DB인가? (member_db 주의)
- [ ] `@Table(name = "...")` 어노테이션으로 테이블명 명시했는가?
- [ ] 연관관계 설정 시 Cascade, orphanRemoval 옵션 확인했는가?
- [ ] N+1 방지를 위한 Fetch Join 쿼리가 필요한가?

### RabbitMQ 메시지 변경 시
- [ ] Producer와 Consumer의 DTO가 동기화되어 있는가?
- [ ] Exchange/Queue/RoutingKey 상수가 `RabbitConfig`에 정의되어 있는가?
- [ ] JSON 직렬화/역직렬화 테스트했는가?

### 인증/보안 관련
- [ ] JWT 토큰 타입("access" vs "refresh") 검증이 있는가?
- [ ] 비밀번호가 BCrypt로 인코딩되는가?
- [ ] 환경변수로 관리해야 할 값이 하드코딩되어 있지 않은가?
- [ ] `.env` 또는 `secrets.yaml`에 실제 비밀번호가 커밋되지 않았는가?

### 테스트
- [ ] 새 기능에 대한 단위 테스트가 있는가?
- [ ] Mock을 사용해 외부 의존성을 격리했는가?
- [ ] 예외 케이스 테스트가 있는가?

### Docker/K8s 변경 시
- [ ] Dockerfile 빌드 테스트했는가?
- [ ] ConfigMap/Secrets에 필요한 환경변수가 있는가?
- [ ] Health check 설정이 적절한가?

---

## 섹션 14: 스테이징/프로덕션 배포

### 14.1 CI/CD 파이프라인 (GitHub Actions)

```yaml
# .github/workflows/ci.yml
# main 브랜치 push 시 자동 실행
# 1. AWS 인증
# 2. ECR 로그인
# 3. ECR 레포지토리 자동 생성
# 4. Docker 빌드 & ECR 푸시

# 각 서비스별 matrix 빌드:
services:
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

### 14.2 Docker Compose 스테이징 분리

```bash
# 로컬 개발
docker-compose up

# 스테이징 (별도 compose 파일 필요 시)
docker-compose -f docker-compose.yml -f docker-compose.staging.yml up
```

### 14.3 배포 전 확인 체크리스트

**인프라 확인**:
- [ ] MySQL 4개 DB 정상 동작 (3306, 3307, 3308, 3309)
- [ ] RabbitMQ 정상 동작 (5672, 15672)
- [ ] Eureka Server 정상 동작 (8761)

**서비스 헬스체크**:
```bash
# 각 서비스 actuator 헬스체크
curl http://localhost:8004/actuator/health  # member-service
curl http://localhost:8002/actuator/health  # order-service
curl http://localhost:8008/actuator/health  # inventory-service

# Eureka 등록 확인
curl http://localhost:8761/eureka/apps

# Gateway 라우팅 확인
curl http://localhost:8000/api/products  # product-service로 라우팅
```

**서비스 간 통신 확인**:
```bash
# RabbitMQ Queue 확인
curl -u guest:guest http://localhost:15672/api/queues

# 주문 생성 → 재고 차감 흐름 테스트
curl -X POST http://localhost:8000/api/order/place \
  -H "Content-Type: application/json" \
  -d '{"customerName":"테스트","request":"테스트"}'
```

### 14.4 롤백 방법

**Docker Compose 환경**:
```bash
# 이전 이미지로 롤백
docker-compose down
docker-compose pull  # 또는 이전 태그 지정
docker-compose up -d
```

**Kubernetes 환경**:
```bash
# Deployment 롤백
kubectl rollout undo deployment/order-service -n tori-app

# 특정 리비전으로 롤백
kubectl rollout history deployment/order-service -n tori-app
kubectl rollout undo deployment/order-service -n tori-app --to-revision=2

# ArgoCD 사용 시
# ArgoCD UI에서 이전 커밋으로 Sync
```

---

## 섹션 15: AI 위반 사례 기록

> AI가 실수할 때마다 여기에 추가하세요. 같은 실수를 반복하지 않기 위함입니다.

### 기록 포맷
```markdown
### YYYY-MM-DD: [서비스명] 실수 내용
- **무엇을 했는지**:
- **왜 잘못인지**:
- **올바른 방법**:
```

### 예시

```markdown
### 2024-XX-XX: [order-service] RabbitMQ 메시지 DTO 불일치
- **무엇을 했는지**: OrderStockMessage에 새 필드 추가 후 inventory-service의 OrderStockRequestDto 수정 누락
- **왜 잘못인지**: JSON 역직렬화 실패로 재고 차감 안됨
- **올바른 방법**: 양쪽 DTO 동시 수정, 필드명 정확히 일치 확인

### 2024-XX-XX: [gateway-service] 라우팅 순서 오류
- **무엇을 했는지**: /api/admin/** 라우트를 /api/admin/products/** 라우트보다 먼저 배치
- **왜 잘못인지**: /api/admin/products 요청이 admin-service로 잘못 라우팅됨
- **올바른 방법**: 구체적인 경로(/api/admin/products/**)를 먼저, 일반적인 경로(/api/admin/**)를 나중에 배치
```

---

*이 문서의 마지막 업데이트: 2024*