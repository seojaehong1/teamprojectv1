## 1. 인증 관련 뷰

### 로그인 페이지 (`/login`)
- **API**: `POST /api/auth/login`
- **기능**:
  - 아이디, 비밀번호 입력
  - 로그인 요청
  - JWT 토큰 저장 (localStorage/sessionStorage)
  - 로그인 성공 시 홈으로 리다이렉트
  - 로그인 실패 시 에러 메시지 표시

### 회원가입 페이지 (`/register`)
- **API들**:
  - `GET /api/auth/check-userid` - 아이디 중복 체크
  - `GET /api/auth/check-email` - 이메일 중복 체크
  - `POST /api/auth/send-verification` - 이메일 인증코드 발송
  - `POST /api/auth/verify-email` - 이메일 인증코드 검증
  - `POST /api/auth/register` - 회원가입
- **기능**:
  - 회원 정보 입력 폼 (이름, 아이디, 비밀번호, 이메일, 생년월일, 전화번호)
  - 아이디 중복 체크 (실시간 또는 버튼 클릭)
  - 이메일 중복 체크
  - 이메일 인증코드 발송 버튼
  - 인증코드 입력 및 검증
  - 회원가입 요청
  - 성공 시 로그인 페이지로 리다이렉트

### 비밀번호 재설정 페이지 (`/reset-password`)
- **API들**:
  - `POST /api/auth/verify-user` - 본인 확인
  - `POST /api/auth/reset-password` - 비밀번호 재설정
- **기능**:
  - 아이디, 이메일 입력 (본인 확인)
  - 본인 확인 요청
  - 새 비밀번호 입력
  - 비밀번호 재설정 요청

---

## 2. 상품 관련 뷰

### 상품 목록 페이지 (`/products` 또는 `/menu`)
- **API들**:
  - `GET /api/products` - 전체 상품 목록 조회
  - `GET /api/menu/{menuCode}/options` - 메뉴 옵션 조회
- **기능**:
  - 상품 목록 표시 (카드 형태 또는 리스트)
  - 카테고리별 필터링 (선택사항)
  - 상품 클릭 시 상세 정보 표시
  - 옵션 선택 모달/팝업
  - 장바구니 추가 버튼

### 상품 상세 페이지 (모달 또는 별도 페이지)
- **API들**:
  - `GET /api/products/{id}` - 특정 상품 조회
  - `GET /api/menu/{menuCode}/options` - 메뉴 옵션 조회
- **기능**:
  - 상품 상세 정보 표시
  - 옵션 그룹별 선택 UI (샷선택, 당도선택 등)
  - 수량 선택
  - 가격 계산 (기본가 + 옵션가)
  - 장바구니 추가 버튼

### 상품 관리 페이지 (`/admin/products`)
- **API들**:
  - `GET /api/products` - 전체 상품 목록 조회
  - `GET /api/products/{id}` - 특정 상품 조회
  - `POST /api/products` - 상품 추가
  - `PUT /api/products/{id}` - 상품 수정
  - `DELETE /api/products/{id}` - 상품 삭제
- **기능**:
  - 상품 목록 테이블 표시
  - 상품 추가 폼 (상품 코드, 이름, 설명, 가격 등)
  - 상품 정보 수정
  - 상품 삭제 버튼
  - JWT 토큰 필요 (ADMIN 권한 권장)
- **참고**: 현재 API는 TODO 상태로 실제 저장/수정/삭제 로직이 미구현입니다.

---

## 3. 주문 관련 뷰

### 장바구니 페이지 (`/orders` 또는 `/cart`)
- **API들**:
  - `POST /orders/add` - 장바구니에 상품 추가
  - (장바구니 조회 API가 있다면 사용)
- **기능**:
  - 장바구니 아이템 목록 표시
  - 각 아이템의 수량 수정
  - 옵션 정보 표시
  - 개별 아이템 삭제
  - 총 금액 계산 및 표시
  - 주문하기 버튼

### 주문하기 (`/orders/place`)
- **API**: `POST /orders/place`
- **기능**:
  - 주문 요청
  - 주문 완료 메시지 표시
  - 주문 ID 및 총 금액 표시
  - 주문 완료 후 장바구니 비우기
  - (비동기로 재고 차감 메시지 발송됨)

---

## 4. 게시판 관련 뷰

### 게시판 목록 페이지 (`/boards`)
- **API**: `GET /api/boards`
- **기능**:
  - 게시글 목록 표시
  - 상단 고정 게시글 우선 표시
  - 작성자, 작성일, 좋아요 수 표시
  - 게시글 작성 버튼 (로그인 시에만)
  - 게시글 클릭 시 상세 페이지로 이동
  - 페이지네이션 (선택사항)

### 게시글 상세 페이지 (`/boards/{id}`)
- **API들**:
  - `GET /api/boards/{id}` - 게시글 상세 조회
  - `GET /api/comments/board/{boardId}` - 댓글 목록 조회
  - `POST /api/comments` - 댓글 작성
  - `DELETE /api/comments/{id}` - 댓글 삭제
  - `POST /api/boards/{id}/like` - 좋아요
- **기능**:
  - 게시글 상세 내용 표시
  - 작성자, 작성일 표시
  - 좋아요 버튼 및 좋아요 수 표시
  - 수정/삭제 버튼 (작성자만 표시)
  - 댓글 목록 표시
  - 댓글 작성 폼 (로그인 시에만)
  - 댓글 삭제 버튼 (작성자 또는 관리자만)

### 게시글 작성 페이지 (`/boards/write`)
- **API**: `POST /api/boards`
- **기능**:
  - 제목, 내용 입력 폼
  - 작성 버튼
  - JWT 토큰 필요 (인증)

### 게시글 수정 페이지 (`/boards/{id}/edit`)
- **API들**:
  - `GET /api/boards/{id}` - 게시글 조회
  - `PUT /api/boards/{id}` - 게시글 수정
- **기능**:
  - 기존 게시글 내용 로드
  - 제목, 내용 수정 폼
  - 수정 버튼
  - 작성자만 수정 가능

---

## 5. 관리자 관련 뷰

### 관리자 메인 페이지 (`/admin`)
- **기능**:
  - 관리자 메뉴 네비게이션
  - 회원 관리 링크
  - 주문 관리 링크
  - 게시판 관리 링크

### 회원 관리 페이지 (`/admin/users`)
- **API들**:
  - `GET /api/admin/users` - 전체 회원 목록 조회
  - `PUT /api/admin/users/{id}` - 회원 정보 수정
  - `DELETE /api/admin/users/{id}` - 회원 삭제
- **기능**:
  - 회원 목록 테이블 표시
  - 회원 정보 수정 (이름, 이메일, 권한)
  - 회원 삭제 버튼
  - JWT 토큰 필요 (ADMIN 권한)

### 주문 관리 페이지 (`/admin/orders`)
- **API들**:
  - `GET /admin` - 전체 주문 목록 조회
  - `GET /admin/orders/{orderId}` - 주문 상세 조회
  - `POST /admin/reset` - 주문 데이터 초기화
- **기능**:
  - 주문 목록 테이블 표시
  - 주문 상세 정보 조회
  - 주문 데이터 초기화 버튼

---

## 6. 고객 관련 뷰

### 고객 관리 페이지 (`/customers`)
- **API들**:
  - `GET /api/customers` - 전체 고객 목록 조회
  - `GET /api/customers/{id}` - 특정 고객 조회
  - `POST /api/customers` - 고객 등록
  - `PUT /api/customers/{id}` - 고객 정보 수정
  - `DELETE /api/customers/{id}` - 고객 삭제
- **기능**:
  - 고객 목록 표시
  - 고객 등록 폼
  - 고객 정보 수정
  - 고객 삭제

---

## 7. 메시지 관련 뷰

### 메시지 페이지 (`/messages` 또는 `/rbmq1`)
- **API들**:
  - `GET /api/messages` - 전체 메시지 조회
  - `POST /api/messages` - 메시지 전송
- **기능**:
  - 메시지 목록 표시
  - 메시지 전송 폼
  - RabbitMQ를 통한 메시지 전송

---

## 8. 재고 관리 관련 뷰

### 재고 관리 페이지 (`/admin/inventory`)
- **API들** (현재 미구현, 추후 추가 필요):
  - `GET /api/inventory/materials` - 전체 재료 목록 조회
  - `GET /api/inventory/materials/{id}` - 특정 재료 조회
  - `PUT /api/inventory/materials/{id}` - 재고 수량 수정
  - `GET /api/inventory/materials/low-stock` - 재고 부족 재료 조회
- **기능**:
  - 재료 목록 테이블 표시 (재료명, 현재 재고량, 단위)
  - 재고 수량 수정 (입고/출고)
  - 재고 부족 알림 표시
  - 재고 이력 조회 (선택사항)
  - JWT 토큰 필요 (ADMIN 권한 권장)

---

## 9. 공통 기능

### 네비게이션 바
- **기능**:
  - 로그인 상태 표시
  - 로그인/로그아웃 버튼
  - 메뉴 링크 (홈, 상품, 주문, 게시판, 관리자)
  - 사용자명 표시 (로그인 시)

### JWT 토큰 관리
- **기능**:
  - 로그인 시 토큰 저장
  - API 요청 시 토큰 헤더에 포함
  - 토큰 만료 시 자동 로그아웃
  - 로그아웃 시 토큰 삭제

### 에러 처리
- **기능**:
  - API 에러 메시지 표시
  - 네트워크 에러 처리
  - 401 에러 시 로그인 페이지로 리다이렉트
  - 403 에러 시 권한 없음 메시지 표시

---

## 뷰별 API 매핑 요약

| 뷰 | 주요 API | 인증 필요 |
|---|---|---|
| 로그인 | POST /api/auth/login | ❌ |
| 회원가입 | POST /api/auth/register | ❌ |
| 비밀번호 재설정 | POST /api/auth/reset-password | ❌ |
| 상품 목록 | GET /api/products | ❌ |
| 상품 상세 | GET /api/products/{id} | ❌ |
| 장바구니 추가 | POST /orders/add | ❌ |
| 주문하기 | POST /orders/place | ❌ |
| 게시판 목록 | GET /api/boards | ❌ |
| 게시글 상세 | GET /api/boards/{id} | ❌ |
| 게시글 작성 | POST /api/boards | ✅ |
| 게시글 수정 | PUT /api/boards/{id} | ✅ |
| 게시글 삭제 | DELETE /api/boards/{id} | ✅ |
| 댓글 작성 | POST /api/comments | ✅ |
| 댓글 삭제 | DELETE /api/comments/{id} | ✅ |
| 좋아요 | POST /api/boards/{id}/like | ✅ |
| 회원 관리 | GET /api/admin/users | ✅ (ADMIN) |
| 주문 관리 | GET /admin | ✅ |
| 상품 관리 | GET /api/products | ✅ (ADMIN 권장) |
| 재고 관리 | GET /api/inventory/materials | ✅ (ADMIN 권장) |
| 고객 관리 | GET /api/customers | ❌ |
| 메시지 전송 | POST /api/messages | ❌ |

---

**참고**: 
- ✅ = JWT 토큰 필요
- ❌ = 인증 불필요
- (ADMIN) = 관리자 권한 필요

