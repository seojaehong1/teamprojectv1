# 스프링부트 MSA 프로젝트

## 프로젝트 소개
이 프로젝트는 스프링부트 3.1.5를 기반으로 한 마이크로서비스 아키텍처(MSA) 예제입니다.

## 기술 스택
- Spring Boot 3.1.5
- JDK 17
- Gradle
- Spring Cloud Gateway (WebFlux 기반)
- Eureka Server
- H2 Database
- JavaScript (Frontend)
- JWT (JSON Web Token)
- BCrypt (비밀번호 암호화)

## 아키텍처 특징
### Spring Cloud Gateway
- Netty 기반의 비동기, 논블로킹(Non-blocking) 아키텍처
- Project Reactor와 Spring WebFlux 기반
- WebFlux를 사용하여 높은 동시성 처리
- 적은 리소스로 많은 요청 처리 가능

### WebFlux vs Web MVC
- WebFlux: 비동기, 논블로킹 방식
- Web MVC: 동기식, 서블릿 기반
- Gateway 서비스는 WebFlux 사용 필수

## 프로젝트 구조
```
sb3_msa3/
├── gateway-service (포트: 8000)
├── eureka-server (포트: 8761)
├── product-service (포트: 8001)
└── order-service (포트: 8002)
```

## 서비스 설명
1. Gateway Service (8000)
   - API Gateway 역할
   - 프론트엔드 UI 제공
   - 서비스 라우팅
   - 인증/인가 처리 (JWT + Interceptor)
   - 로그인 서비스 제공
   - WebFlux 기반의 비동기 처리

2. Eureka Server (8761)
   - 서비스 디스커버리
   - 서비스 등록/관리

3. Product Service (8001)
   - 상품 관리 서비스
   - CRUD API 제공

4. Order Service (8002)
   - 주문 관리 서비스
   - CRUD API 제공

## 실행 방법
1. 프로젝트 클론
2. 각 서비스 디렉토리에서 다음 명령어 실행:
   ```bash
   ./gradlew bootRun
   ```
3. 브라우저에서 http://localhost:8000 접속

## 로그인 서비스
### 테스트 계정
- 관리자 계정
  - 사용자명: admin
  - 비밀번호: admin123
- 일반 사용자 계정
  - 사용자명: user1
  - 비밀번호: user123
  - 사용자명: user2
  - 비밀번호: user123

### 주요 기능
- JWT 기반 인증
- 인터셉터를 통한 인증 처리
- BCrypt를 사용한 비밀번호 암호화
- H2 데이터베이스 사용
- 부트스트랩 기반의 모던한 UI
- WebFlux 기반의 비동기 처리

### API 엔드포인트
- POST /api/auth/login - 로그인
  - Request Body: { "username": "string", "password": "string" }
  - Response: { "token": "string", "username": "string", "role": "string" }

## API 엔드포인트
### Product Service
- GET /api/products - 상품 목록 조회
- GET /api/products/{id} - 상품 상세 조회
- POST /api/products - 상품 등록
- PUT /api/products/{id} - 상품 수정
- DELETE /api/products/{id} - 상품 삭제

### Order Service
- GET /api/orders - 주문 목록 조회
- GET /api/orders/{id} - 주문 상세 조회
- POST /api/orders - 주문 등록
- PUT /api/orders/{id} - 주문 수정
- DELETE /api/orders/{id} - 주문 삭제 