# MSA Coffee Shop Management System

Spring Boot + Spring Cloud 기반의 마이크로서비스 아키텍처(MSA) 카페 관리 시스템입니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.1.5, Spring Cloud 2022.0.4, Java 17 |
| Frontend | Thymeleaf, HTML/CSS/JavaScript |
| Database | H2 (개발), MySQL (운영) |
| Message Queue | RabbitMQ |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Auth | Spring Security, JWT, OAuth2 (Google, Naver) |
| Build | Gradle (Multi-module) |
| Container | Kubernetes (마이그레이션 브랜치) |

---

## 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| `eureka-server` | 8761 | 서비스 디스커버리 |
| `gateway-service` | 8000 | API 게이트웨이 & 라우팅 |
| `frontend-service` | 8005 | 웹 UI (Thymeleaf) |
| `member-service` | 8004 | 회원가입, 로그인, JWT, OAuth2 |
| `product-service` | 8001 | 상품/메뉴 관리 |
| `order-service` | 8002 | 주문 및 장바구니 |
| `cust-service` | 8003 | 고객 문의 및 메시지 |
| `board-service` | 8006 | 게시판 및 댓글 |
| `inventory-service` | 8008 | 재고 및 자재 관리 |

---

## 브랜치 전략

### `main`
- 안정적인 배포 가능 상태의 코드
- 컨트롤러 분리 및 ApiClient 유틸 추가 완료
- H2 인메모리 DB 기반

### `develop`
- 통합 개발 브랜치
- 각 기능 브랜치가 병합되는 기준점

### `developV2` ~ `developV9`
- 단계별 개발 버전 브랜치
- 각 버전별 기능 추가 및 개선 이력 관리

| 브랜치 | 설명 |
|--------|------|
| `developV2` | 2차 개발 버전 |
| `developV3` | 3차 개발 버전 |
| `developV4` | 4차 개발 버전 |
| `developV5` | 5차 개발 버전 |
| `developV6` | 6차 개발 버전 |
| `developV7` | 7차 개발 버전 |
| `developV8` | 8차 개발 버전 |
| `developV9` | 9차 개발 버전 |
| `developPack` | 패키지 구조 정리 버전 |
| `developha` | 추가 개발 버전 |

### Feature 브랜치

| 브랜치 | 설명 |
|--------|------|
| `feature/controller-refactoring` | 컨트롤러 분리 및 ApiClient 유틸 리팩토링 (main 병합 완료) |
| `feature/mysql-migration` | H2 → MySQL 데이터베이스 마이그레이션 |
| `feature/kubernetes-migration` | Kubernetes 배포 환경 구성 |

### Test 브랜치

| 브랜치 | 설명 |
|--------|------|
| `test` | 테스트 브랜치 |
| `test2` | 테스트 브랜치 2 |
| `20260112test` | 날짜 기반 테스트 브랜치 |

---

## 주요 기능

### 사용자
- 회원가입 (이메일 인증) / 로그인 (JWT)
- 소셜 로그인 (Google, Naver)
- 메뉴 조회 및 옵션 선택
- 장바구니 및 주문
- 주문 내역 조회
- 게시판 글쓰기 / 댓글 / 좋아요
- 고객 문의

### 관리자 (Admin)
- 회원 관리
- 상품 등록/수정/삭제
- 주문 관리
- 공지사항 / 이벤트 관리
- 고객 문의 처리

### 점주 (Owner)
- 재고 및 자재 관리
- 레시피 관리
- 옵션 마스터 관리

---

## API 게이트웨이 라우팅

```
/api/products/**    → product-service
/api/orders/**      → order-service
/api/customers/**   → cust-service
/api/messages/**    → cust-service
/api/auth/**        → member-service
/api/admin/**       → member-service
/api/boards/**      → board-service
/api/comments/**    → board-service
/api/inventory/**   → inventory-service
/**                 → frontend-service (fallback)
```

---

## 실행 방법

### 사전 요구사항
- Java 17
- RabbitMQ (localhost:5672)

### 실행 순서

```bash
# 1. Eureka Server 실행
cd eureka-server && ./gradlew bootRun

# 2. Gateway Service 실행
cd gateway-service && ./gradlew bootRun

# 3. 각 마이크로서비스 실행 (순서 무관)
cd member-service && ./gradlew bootRun
cd product-service && ./gradlew bootRun
cd order-service && ./gradlew bootRun
cd cust-service && ./gradlew bootRun
cd board-service && ./gradlew bootRun
cd inventory-service && ./gradlew bootRun

# 4. Frontend Service 실행
cd frontend-service && ./gradlew bootRun
```

### 접속
- 서비스: http://localhost:8000
- Eureka Dashboard: http://localhost:8761

---

## 프로젝트 구조

```
teamprojectv1/
├── eureka-server/          # 서비스 디스커버리
├── gateway-service/        # API 게이트웨이
├── frontend-service/       # 웹 UI (32개 HTML 템플릿)
├── member-service/         # 회원/인증 서비스
├── product-service/        # 상품 서비스
├── order-service/          # 주문 서비스
├── cust-service/           # 고객 서비스
├── board-service/          # 게시판 서비스
├── inventory-service/      # 재고 서비스
├── API_DOCUMENTATION.md    # API 문서
├── API-MAPPING.md          # API 매핑 가이드
└── VIEW_FEATURES.md        # 프론트엔드 기능 명세
```

---

## 문서

- [API Documentation](./API_DOCUMENTATION.md) - 전체 API 레퍼런스
- [API Mapping](./API-MAPPING.md) - 프론트엔드-백엔드 API 매핑
- [View Features](./VIEW_FEATURES.md) - 프론트엔드 기능 명세