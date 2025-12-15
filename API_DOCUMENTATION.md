# MSA 프로젝트 API 문서

이 문서는 MSA 프로젝트의 모든 API 엔드포인트를 정리한 문서입니다.

## 목차
1. [Gateway Service](#gateway-service)
2. [Member Service](#member-service)
3. [Product Service](#product-service)
4. [Order Service](#order-service)
5. [Board Service](#board-service)
6. [Customer Service](#customer-service)
7. [Inventory Service](#inventory-service)

---

## Gateway Service

**포트**: 8000  
**역할**: API Gateway - 모든 서비스의 진입점

Gateway를 통해 라우팅되는 모든 API는 `http://localhost:8000`을 기본 URL로 사용합니다.

---

## Member Service

**포트**: 8004  
**서비스명**: member-service

### 인증 API (`/api/auth`)

#### 1. 아이디 중복 체크
- **엔드포인트**: `GET /api/auth/check-userid`
- **설명**: 회원가입 시 아이디 중복 여부 확인
- **파라미터**: 
  - `userId` (query parameter, required): 확인할 아이디
- **응답**: 
  ```json
  {
    "exists": true/false
  }
  ```

#### 2. 이메일 중복 체크
- **엔드포인트**: `GET /api/auth/check-email`
- **설명**: 회원가입 시 이메일 중복 여부 확인
- **파라미터**: 
  - `email` (query parameter, required): 확인할 이메일
- **응답**: 
  ```json
  {
    "exists": true/false
  }
  ```

#### 3. 이메일 인증코드 발송
- **엔드포인트**: `POST /api/auth/send-verification`
- **설명**: 회원가입 시 이메일 인증코드 발송
- **요청 본문**: 
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "인증코드가 발송되었습니다."
  }
  ```

#### 4. 이메일 인증코드 검증
- **엔드포인트**: `POST /api/auth/verify-email`
- **설명**: 발송된 인증코드 검증
- **요청 본문**: 
  ```json
  {
    "email": "user@example.com",
    "code": "123456"
  }
  ```
- **응답**: 
  ```json
  {
    "verified": true/false,
    "message": "인증코드가 일치하지 않거나 만료되었습니다." // verified가 false일 때만
  }
  ```

#### 5. 회원가입
- **엔드포인트**: `POST /api/auth/register`
- **설명**: 신규 회원 등록
- **요청 본문**: 
  ```json
  {
    "username": "홍길동",
    "userId": "hong123",
    "password": "password123",
    "email": "hong@example.com",
    "birthDate": "1990-01-01",
    "phoneNum": "010-1234-5678"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "회원 가입이 완료되었습니다.",
    "username": "홍길동"
  }
  ```

#### 6. 로그인
- **엔드포인트**: `POST /api/auth/login`
- **설명**: 사용자 로그인 및 JWT 토큰 발급
- **요청 본문**: 
  ```json
  {
    "userId": "hong123",
    "password": "password123"
  }
  ```
- **응답**: 
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "홍길동",
    "userId": "hong123",
    "role": "USER"
  }
  ```

#### 7. 본인 확인
- **엔드포인트**: `POST /api/auth/verify-user`
- **설명**: 비밀번호 재설정 전 아이디와 이메일로 본인 확인
- **요청 본문**: 
  ```json
  {
    "userId": "hong123",
    "email": "hong@example.com"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "본인 확인이 완료되었습니다."
  }
  ```

#### 8. 비밀번호 재설정
- **엔드포인트**: `POST /api/auth/reset-password`
- **설명**: 비밀번호 변경
- **요청 본문**: 
  ```json
  {
    "userId": "hong123",
    "newPassword": "newPassword123"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "비밀번호가 변경되었습니다."
  }
  ```

### 관리자 API (`/api/admin/users`)

**인증**: JWT 토큰 필요 (Authorization: Bearer {token})  
**권한**: ADMIN 역할 필요

#### 1. 전체 회원 목록 조회
- **엔드포인트**: `GET /api/admin/users`
- **설명**: 관리자가 전체 회원 목록 조회
- **헤더**: 
  - `Authorization: Bearer {JWT_TOKEN}` (required)
- **응답**: 
  ```json
  [
    {
      "id": 1,
      "username": "홍길동",
      "email": "hong@example.com",
      "role": "USER"
    }
  ]
  ```

#### 2. 회원 정보 수정
- **엔드포인트**: `PUT /api/admin/users/{id}`
- **설명**: 관리자가 회원 정보 수정
- **헤더**: 
  - `Authorization: Bearer {JWT_TOKEN}` (required)
- **경로 변수**: 
  - `id` (Long): 회원 ID
- **요청 본문**: 
  ```json
  {
    "username": "홍길동",
    "email": "hong@example.com",
    "role": "ADMIN"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "사용자 정보가 업데이트되었습니다.",
    "id": 1,
    "username": "홍길동",
    "email": "hong@example.com",
    "role": "ADMIN"
  }
  ```

#### 3. 회원 삭제
- **엔드포인트**: `DELETE /api/admin/users/{id}`
- **설명**: 관리자가 회원 삭제
- **헤더**: 
  - `Authorization: Bearer {JWT_TOKEN}` (required)
- **경로 변수**: 
  - `id` (Long): 회원 ID
- **응답**: 
  ```json
  {
    "message": "사용자가 삭제되었습니다."
  }
  ```

---

## Product Service

**포트**: 8001  
**서비스명**: product-service

### 상품 API (`/api/products`)

#### 1. 전체 상품 목록 조회
- **엔드포인트**: `GET /api/products`
- **설명**: 모든 상품(메뉴) 목록 조회
- **응답**: 
  ```json
  [
    {
      "id": "cof-001",
      "name": "아메리카노",
      "description": "커피",
      "price": 2500,
      "stock": 0
    }
  ]
  ```

#### 2. 특정 상품 조회
- **엔드포인트**: `GET /api/products/{id}`
- **설명**: 특정 상품(메뉴) 상세 정보 조회
- **경로 변수**: 
  - `id` (String): 상품 코드 (예: "cof-001")
- **응답**: 
  ```json
  {
    "id": "cof-001",
    "name": "아메리카노",
    "description": "커피",
    "price": 2500,
    "stock": 0
  }
  ```

#### 3. 상품 추가
- **엔드포인트**: `POST /api/products`
- **설명**: 새로운 상품(메뉴) 추가
- **요청 본문**: 
  ```json
  {
    "id": "cof-002",
    "name": "카페라떼",
    "description": "커피",
    "price": 3000,
    "stock": 0
  }
  ```
- **참고**: 현재는 TODO 상태로 실제 저장 로직 미구현

#### 4. 상품 수정
- **엔드포인트**: `PUT /api/products/{id}`
- **설명**: 상품(메뉴) 정보 수정
- **경로 변수**: 
  - `id` (String): 상품 코드
- **요청 본문**: 
  ```json
  {
    "id": "cof-001",
    "name": "아메리카노",
    "description": "커피",
    "price": 2800,
    "stock": 0
  }
  ```
- **참고**: 현재는 TODO 상태로 실제 업데이트 로직 미구현

#### 5. 상품 삭제
- **엔드포인트**: `DELETE /api/products/{id}`
- **설명**: 상품(메뉴) 삭제
- **경로 변수**: 
  - `id` (String): 상품 코드
- **참고**: 현재는 TODO 상태로 실제 삭제 로직 미구현

### 메뉴 옵션 API

#### 1. 메뉴 옵션 조회
- **엔드포인트**: `GET /api/menu/{menuCode}/options`
- **설명**: 특정 메뉴의 옵션 그룹별 조회
- **경로 변수**: 
  - `menuCode` (String): 메뉴 코드 (예: "cof-001")
- **응답**: 
  ```json
  {
    "샷선택": [
      {
        "optionId": 2,
        "optionName": "샷추가(+600)",
        "optionPrice": 600,
        "optionGroupName": "샷선택"
      }
    ],
    "당도선택": [
      {
        "optionId": 6,
        "optionName": "바닐라시럽추가(+500)",
        "optionPrice": 500,
        "optionGroupName": "당도선택"
      }
    ]
  }
  ```

---

## Order Service

**포트**: 8002  
**서비스명**: order-service

### 주문 API (`/orders`)

#### 1. 장바구니에 상품 추가
- **엔드포인트**: `POST /orders/add`
- **설명**: 장바구니에 상품과 옵션을 추가
- **요청 본문**: 
  ```json
  [
    {
      "customerId": 1,
      "menuCode": "cof-001",
      "menuName": "아메리카노",
      "quantity": 1,
      "unitPrice": 2500,
      "totalAmount": 3600,
      "options": [
        {
          "optionId": 2,
          "optionName": "샷추가(+600)",
          "optionPrice": 600,
          "optionGroupName": "샷선택"
        },
        {
          "optionId": 6,
          "optionName": "바닐라시럽추가(+500)",
          "optionPrice": 500,
          "optionGroupName": "당도선택"
        }
      ]
    }
  ]
  ```
- **응답**: 
  ```
  장바구니 (ID: 1)에 상품 1개와 2개의 옵션이 성공적으로 저장되었습니다.
  ```

#### 2. 주문하기
- **엔드포인트**: `POST /orders/place`
- **설명**: 장바구니의 상품들을 주문으로 전환. 주문 완료 후 RabbitMQ를 통해 재고 차감 메시지를 자동으로 발송합니다.
- **비동기 처리**: 주문 저장 후 재고 차감은 RabbitMQ를 통해 비동기로 처리됩니다.
- **응답**: 
  ```
  주문이 성공적으로 완료되었습니다. 주문 ID: 1, 총 결제 금액: 10,600원
  ```
- **참고**: 재고 차감은 주문 완료와 독립적으로 처리되며, 재고 부족 시에도 주문은 완료됩니다 (재고 차감 실패는 로그에 기록됨).

### 관리자 주문 API (`/admin`)

#### 1. 전체 주문 목록 조회
- **엔드포인트**: `GET /admin`
- **설명**: 관리자가 모든 주문 목록 조회 (HTML 페이지 반환)
- **응답**: HTML 페이지 (admin/order-list.html)

#### 2. 주문 상세 조회
- **엔드포인트**: `GET /admin/orders/{orderId}`
- **설명**: 특정 주문의 상세 정보 조회 (HTML 페이지 반환)
- **경로 변수**: 
  - `orderId` (Integer): 주문 ID
- **응답**: HTML 페이지 (admin/order-detail.html)

#### 3. 주문 데이터 초기화
- **엔드포인트**: `POST /admin/reset`
- **설명**: 모든 주문 데이터 삭제 (관리자 전용)
- **응답**: 리다이렉트 (주문 목록 페이지로 이동)

---

## Board Service

**포트**: 8006  
**서비스명**: board-service

### 게시판 API (`/api/boards`)

**인증**: 대부분의 API는 JWT 토큰 필요 (Authorization: Bearer {token})

#### 1. 전체 게시글 조회
- **엔드포인트**: `GET /api/boards`
- **설명**: 모든 게시글 목록 조회
- **인증**: 불필요
- **응답**: 
  ```json
  [
    {
      "id": 1,
      "title": "게시글 제목",
      "content": "게시글 내용",
      "author": "user123",
      "createdAt": "2024-01-01T10:00:00",
      "isPinned": false,
      "likeCount": 0
    }
  ]
  ```

#### 2. 게시글 상세 조회
- **엔드포인트**: `GET /api/boards/{id}`
- **설명**: 특정 게시글 상세 정보 조회
- **인증**: 불필요
- **경로 변수**: 
  - `id` (Long): 게시글 ID
- **응답**: 
  ```json
  {
    "id": 1,
    "title": "게시글 제목",
    "content": "게시글 내용",
    "author": "user123",
    "createdAt": "2024-01-01T10:00:00",
    "isPinned": false,
    "likeCount": 0
  }
  ```

#### 3. 게시글 작성
- **엔드포인트**: `POST /api/boards`
- **설명**: 새로운 게시글 작성
- **인증**: JWT 토큰 필요
- **요청 본문**: 
  ```json
  {
    "title": "게시글 제목",
    "content": "게시글 내용"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "게시글이 작성되었습니다.",
    "boardId": 1
  }
  ```

#### 4. 게시글 수정
- **엔드포인트**: `PUT /api/boards/{id}`
- **설명**: 게시글 수정 (작성자만 가능)
- **인증**: JWT 토큰 필요
- **경로 변수**: 
  - `id` (Long): 게시글 ID
- **요청 본문**: 
  ```json
  {
    "title": "수정된 제목",
    "content": "수정된 내용"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "게시글이 수정되었습니다."
  }
  ```

#### 5. 게시글 삭제
- **엔드포인트**: `DELETE /api/boards/{id}`
- **설명**: 게시글 삭제 (작성자만 가능)
- **인증**: JWT 토큰 필요
- **경로 변수**: 
  - `id` (Long): 게시글 ID
- **응답**: 
  ```json
  {
    "message": "게시글이 삭제되었습니다."
  }
  ```

#### 6. 게시글 상단 고정 토글
- **엔드포인트**: `POST /api/boards/{id}/pin`
- **설명**: 게시글 상단 고정/해제 (관리자 기능)
- **인증**: JWT 토큰 필요
- **경로 변수**: 
  - `id` (Long): 게시글 ID
- **응답**: 
  ```json
  {
    "message": "게시글이 상단에 고정되었습니다.",
    "isPinned": true
  }
  ```

#### 7. 게시글 좋아요
- **엔드포인트**: `POST /api/boards/{id}/like`
- **설명**: 게시글에 좋아요 추가
- **인증**: JWT 토큰 필요
- **경로 변수**: 
  - `id` (Long): 게시글 ID
- **응답**: 
  ```json
  {
    "message": "좋아요!"
  }
  ```

#### 8. 작성자별 게시글 조회
- **엔드포인트**: `GET /api/boards/author/{author}`
- **설명**: 특정 작성자의 게시글 목록 조회
- **인증**: 불필요
- **경로 변수**: 
  - `author` (String): 작성자 ID
- **응답**: 게시글 목록 배열

### 댓글 API (`/api/comments`)

#### 1. 게시글별 댓글 목록 조회
- **엔드포인트**: `GET /api/comments/board/{boardId}`
- **설명**: 특정 게시글의 댓글 목록 조회
- **인증**: 불필요
- **경로 변수**: 
  - `boardId` (Long): 게시글 ID
- **응답**: 
  ```json
  [
    {
      "id": 1,
      "boardId": 1,
      "content": "댓글 내용",
      "author": "user123",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
  ```

#### 2. 댓글 작성
- **엔드포인트**: `POST /api/comments`
- **설명**: 새로운 댓글 작성
- **인증**: JWT 토큰 필요
- **요청 본문**: 
  ```json
  {
    "boardId": 1,
    "content": "댓글 내용"
  }
  ```
- **응답**: 
  ```json
  {
    "message": "댓글이 작성되었습니다.",
    "commentId": 1
  }
  ```

#### 3. 댓글 삭제
- **엔드포인트**: `DELETE /api/comments/{id}`
- **설명**: 댓글 삭제 (작성자 또는 관리자만 가능)
- **인증**: JWT 토큰 필요
- **경로 변수**: 
  - `id` (Long): 댓글 ID
- **응답**: 
  ```json
  {
    "message": "댓글이 삭제되었습니다."
  }
  ```

#### 4. 작성자별 댓글 목록 조회
- **엔드포인트**: `GET /api/comments/author/{author}`
- **설명**: 특정 작성자의 댓글 목록 조회
- **인증**: 불필요
- **경로 변수**: 
  - `author` (String): 작성자 ID
- **응답**: 댓글 목록 배열

---

## Customer Service

**포트**: 8003  
**서비스명**: cust-service

### 고객 API (`/api/customers`)

#### 1. 전체 고객 목록 조회
- **엔드포인트**: `GET /api/customers`
- **설명**: 모든 고객 목록 조회
- **응답**: 
  ```json
  [
    {
      "id": 1,
      "name": "홍길동",
      "email": "hong@example.com",
      "phone": "010-1234-5678"
    }
  ]
  ```

#### 2. 특정 고객 조회
- **엔드포인트**: `GET /api/customers/{id}`
- **설명**: 특정 고객 정보 조회
- **경로 변수**: 
  - `id` (Long): 고객 ID
- **응답**: 
  ```json
  {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678"
  }
  ```

#### 3. 고객 등록
- **엔드포인트**: `POST /api/customers`
- **설명**: 새로운 고객 등록
- **요청 본문**: 
  ```json
  {
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678"
  }
  ```
- **응답**: 등록된 고객 정보

#### 4. 고객 정보 수정
- **엔드포인트**: `PUT /api/customers/{id}`
- **설명**: 고객 정보 수정
- **경로 변수**: 
  - `id` (Long): 고객 ID
- **요청 본문**: 
  ```json
  {
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678"
  }
  ```
- **응답**: 수정된 고객 정보

#### 5. 고객 삭제
- **엔드포인트**: `DELETE /api/customers/{id}`
- **설명**: 고객 삭제
- **경로 변수**: 
  - `id` (Long): 고객 ID
- **응답**: 200 OK (본문 없음)

### 메시지 API (`/api/messages`)

#### 1. 전체 메시지 조회
- **엔드포인트**: `GET /api/messages`
- **설명**: 모든 메시지 목록 조회
- **응답**: 
  ```json
  [
    {
      "id": 1,
      "message": "메시지 내용",
      "timestamp": "2024-01-01T10:00:00"
    }
  ]
  ```

#### 2. 메시지 전송
- **엔드포인트**: `POST /api/messages`
- **설명**: 메시지 전송 (RabbitMQ를 통해 전송 및 DB 저장)
- **요청 본문**: 
  ```json
  {
    "message": "메시지 내용"
  }
  ```
- **응답**: 저장된 메시지 정보

---

## Inventory Service

**포트**: 8008  
**서비스명**: inventory-service

### 재고 API (`/api/inventory`)

#### 1. 주문 재고 처리 (REST API)
- **엔드포인트**: `POST /api/inventory/process`
- **설명**: 주문에 대한 재고 처리 (직접 호출용)
- **요청 본문**: 
  ```json
  {
    "orderId": 1,
    "items": [
      {
        "menuCode": "cof-001",
        "quantity": 2,
        "optionIds": [2, 6]
      }
    ]
  }
  ```
- **응답**: 
  ```json
  {
    "sucess": true,
    "message": "재고 차감 완료",
    "insufficientItems": null
  }
  ```
- **재고 부족 시 응답**: 
  ```json
  {
    "sucess": false,
    "message": "재고가 부족한 재료가 있습니다.",
    "insufficientItems": [
      "커피원두(필요: 10.0, 보유:5.0)"
    ]
  }
  ```

### RabbitMQ 메시지 처리

#### 재고 차감 메시지 수신 (비동기)
- **큐 이름**: `inventory-queue`
- **설명**: order-service에서 주문 완료 시 자동으로 발송되는 재고 차감 메시지를 수신하여 처리합니다.
- **메시지 형식**: 
  ```json
  {
    "orderId": 1,
    "items": [
      {
        "menuCode": "cof-001",
        "quantity": 2,
        "optionIds": [2, 6]
      }
    ]
  }
  ```
- **처리 로직**:
  1. 메뉴 코드별 레시피를 조회하여 기본 재료 필요량 계산
  2. 옵션에 따른 추가/제거/변경 재료 처리
  3. 재고 충분 여부 확인
  4. 재고 부족 시 로그 기록
  5. 재고 충분 시 재고 차감 실행
- **옵션 처리 방식**:
  - **추가**: `toMaterial` 재고 증가
  - **제거**: `fromMaterial` 재고 감소
  - **변경**: `fromMaterial` 감소 + `toMaterial` 증가

---

## 인증 및 권한

### JWT 토큰 사용법

대부분의 API는 JWT 토큰 인증이 필요합니다. 토큰은 로그인 API(`POST /api/auth/login`)를 통해 발급받을 수 있습니다.

**토큰 사용 방법**:
```
Authorization: Bearer {JWT_TOKEN}
```

### 권한 레벨

- **PUBLIC**: 인증 불필요
- **USER**: 로그인한 사용자 (JWT 토큰 필요)
- **ADMIN**: 관리자 권한 필요 (JWT 토큰 + ADMIN 역할)

---

## 에러 응답 형식

일반적인 에러 응답 형식:

```json
{
  "error": "에러 메시지"
}
```

또는 단순 문자열 메시지로 반환될 수 있습니다.

---

## 참고사항

1. **Gateway를 통한 접근**: 모든 API는 Gateway(포트 8000)를 통해 접근하는 것을 권장합니다.
2. **직접 접근**: 각 서비스는 개별 포트로도 접근 가능하지만, Gateway를 통한 라우팅을 권장합니다.
3. **Eureka 서버**: 서비스 디스커버리를 위해 Eureka 서버(포트 8761)가 실행 중이어야 합니다.
4. **RabbitMQ**: 다음 서비스들이 RabbitMQ를 사용하므로 RabbitMQ 서버가 실행 중이어야 합니다.
   - **order-service**: 주문 완료 후 재고 차감 메시지 발송 (Producer)
   - **inventory-service**: 재고 차감 메시지 수신 및 처리 (Consumer)
   - **cust-service**: 메시지 전송 (Producer)
   - **order-service**: 메시지 수신 (Consumer, inventory_final-main 참고)

## 서비스 간 통신 구조

### 동기 통신 (REST API)
- Gateway → 각 마이크로서비스: REST API 호출
- 클라이언트 → Gateway: REST API 호출

### 비동기 통신 (RabbitMQ)
- **order-service → inventory-service**: 
  - 큐: `inventory-queue`
  - 이벤트: 주문 완료 시 재고 차감 요청
  - 처리 방식: 비동기 (주문 완료와 독립적으로 처리)
  
- **cust-service → order-service** (참고용):
  - 큐: `product-queue`
  - 이벤트: 상품 메시지 전송

---

## 서비스 포트 요약

| 서비스 | 포트 | 서비스명 |
|--------|------|----------|
| Gateway | 8000 | gateway-service |
| Product | 8001 | product-service |
| Order | 8002 | order-service |
| Customer | 8003 | cust-service |
| Member | 8004 | member-service |
| Board | 8006 | board-service |
| Inventory | 8008 | inventory-service |
| Eureka | 8761 | eureka-server |

---

**문서 작성일**: 2024년  
**프로젝트**: MSA Project Park

