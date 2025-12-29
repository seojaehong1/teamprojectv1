# 화면-API 매핑 문서

이 문서는 frontend-service의 HTML 템플릿 파일들과 필요한 API를 매핑한 문서입니다.

**작성일**: 2024년  
**프로젝트**: MSA Project Park

---

## 목차

1. [인증 관련](#1-인증-관련)
2. [메뉴 관련](#2-메뉴-관련)
3. [주문 관련](#3-주문-관련)
4. [마이페이지 관련](#4-마이페이지-관련)
5. [게시판 관련](#5-게시판-관련)
6. [관리자 관련](#6-관리자-관련)
7. [점주 관련](#7-점주-관련)
8. [기타](#8-기타)

---

## 1. 인증 관련

### 1.1 login.html (담당: 🔵 하성호)

**화면 설명**: 일반회원/관리자 로그인 페이지. userId 또는 email로 로그인 가능하며, 일반회원 탭과 관리자 탭을 구분하여 처리.

**필요한 API**:

1. **POST /api/auth/login**
   - 용도: 사용자 로그인 및 JWT 토큰 발급
   - Request:
```json
{
  "userId": "user123",
  "password": "password123"
}
```
   - Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "홍길동",
  "userId": "user123",
  "role": "USER"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('users')` - 더미 데이터에서 사용자 검색
- ✅ 하드코딩된 관리자 계정: `admin/admin1234` (관리자 탭 전용)
- ✅ 로그인 성공 시 localStorage에 상태 저장: `isLoggedIn`, `userName`, `userRole`
- ⚠️ 주의사항: userId와 email 모두로 로그인 가능하도록 구현 필요

**연동 시 수정 필요 사항**:
- `login.html` 라인 306-443: `$('#loginForm').on('submit')` 핸들러에서 localStorage 로직을 API 호출로 변경
- 성공 시 JWT 토큰을 localStorage 또는 cookie에 저장
- role에 따라 리다이렉트 경로 결정 (user → index.html, owner → owner/inventory.html, admin → admin/user.html)

---

### 1.2 signup.html (담당: 🔵 하성호)

**화면 설명**: 신규 회원가입 페이지. 이름, 아이디, 비밀번호, 이메일 입력 및 이메일 인증 기능 포함.

**필요한 API**:

1. **GET /api/auth/check-userid?userId={userId}**
   - 용도: 회원가입 시 아이디 중복 체크
   - Request: Query Parameter `userId`
   - Response:
```json
{
  "exists": false
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **POST /api/auth/send-verification**
   - 용도: 이메일 인증번호 발송
   - Request:
```json
{
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "message": "인증코드가 발송되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

3. **POST /api/auth/verify-email**
   - 용도: 발송된 인증번호 검증
   - Request:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```
   - Response:
```json
{
  "verified": true,
  "message": "인증이 완료되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

4. **POST /api/auth/register**
   - 용도: 신규 회원 등록
   - Request:
```json
{
  "name": "홍길동",
  "userId": "user123",
  "password": "password123",
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "message": "회원 가입이 완료되었습니다.",
  "username": "홍길동"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('users')` - 더미 데이터에서 중복 체크 및 저장
- ✅ 더미 인증번호: `authCode = '123456'` (항상 성공)
- ⚠️ 주의사항: 실제 이메일 발송 기능 필요

**연동 시 수정 필요 사항**:
- `signup.html` 라인 393-408: 아이디 blur 이벤트에서 API 호출 추가
- 라인 470-517: 인증번호 발송 버튼 클릭 시 API 호출
- 라인 521-549: 인증번호 확인 시 API 호출
- 라인 561-708: 회원가입 폼 제출 시 API 호출 (localStorage 대신)

---

### 1.3 find_id.html (담당: 🔵 하성호)

**화면 설명**: 아이디 찾기 페이지. 이름과 이메일로 본인 확인 후 아이디 표시.

**필요한 API**:

1. **POST /api/auth/send-verification**
   - 용도: 이메일 인증번호 발송 (아이디 찾기용)
   - Request:
```json
{
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "message": "인증코드가 발송되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **POST /api/auth/verify-email**
   - 용도: 발송된 인증번호 검증
   - Request:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```
   - Response:
```json
{
  "verified": true
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

3. **POST /api/auth/find-userid**
   - 용도: 이름과 이메일로 아이디 찾기
   - Request:
```json
{
  "name": "홍길동",
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "userId": "user123",
  "message": "아이디를 찾았습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('users')` - 더미 데이터에서 검색
- ✅ 더미 인증번호: `authCode = '123456'`
- ⚠️ 주의사항: 실제 이메일 발송 및 본인 확인 절차 필요

**연동 시 수정 필요 사항**:
- `find_id.html` 라인 396-421: 인증번호 발송 시 API 호출
- 라인 425-446: 인증번호 확인 시 API 호출
- 라인 450-493: 아이디 찾기 버튼 클릭 시 API 호출

---

### 1.4 find_password.html (담당: 🔵 하성호)

**화면 설명**: 비밀번호 찾기 페이지. 이름, 아이디, 이메일로 본인 확인 후 비밀번호 재설정 링크 발송.

**필요한 API**:

1. **POST /api/auth/send-verification**
   - 용도: 이메일 인증번호 발송 (비밀번호 찾기용)
   - Request:
```json
{
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "message": "인증코드가 발송되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **POST /api/auth/verify-email**
   - 용도: 발송된 인증번호 검증
   - Request:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```
   - Response:
```json
{
  "verified": true
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

3. **POST /api/auth/verify-user**
   - 용도: 비밀번호 재설정 전 본인 확인 (이름, userId, 이메일)
   - Request:
```json
{
  "userId": "user123",
  "name": "홍길동",
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "message": "본인 확인이 완료되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

4. **POST /api/auth/reset-password**
   - 용도: 비밀번호 재설정 (이메일로 링크 발송 또는 직접 재설정)
   - Request:
```json
{
  "userId": "user123",
  "email": "user@example.com"
}
```
   - Response:
```json
{
  "message": "비밀번호 재설정 링크가 발송되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('users')` - 더미 데이터에서 검색
- ✅ 더미 인증번호: `authCode = '123456'`
- ⚠️ 주의사항: 현재는 비밀번호를 그대로 표시하지만, 실제로는 재설정 링크를 이메일로 발송해야 함

**연동 시 수정 필요 사항**:
- `find_password.html` 라인 408-433: 인증번호 발송 시 API 호출
- 라인 437-458: 인증번호 확인 시 API 호출
- 라인 462-514: 비밀번호 찾기 버튼 클릭 시 API 호출 (본인 확인 후 재설정 링크 발송)

---

## 2. 메뉴 관련

### 2.1 menu/drink.html (담당: 🟢 이주희)

**화면 설명**: 메뉴 목록 페이지. 카테고리 필터링 및 검색 기능 포함.

**필요한 API**:

1. **GET /api/products**
   - 용도: 전체 메뉴 목록 조회
   - Query Parameters: `category` (선택), `search` (선택), `page` (선택), `size` (선택)
   - Response:
```json
[
  {
    "menuCode": 1,
    "menuName": "아메리카노",
    "category": "커피",
    "basePrice": 4500,
    "baseVolume": "Regular",
    "description": "진한 에스프레소에 뜨거운 물을 넣어 깔끔하고 강렬한 맛",
    "isAvailable": true,
    "allergyIds": "1,2"
  }
]
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ 하드코딩된 메뉴 목록 (HTML에 직접 작성)
- ⚠️ 주의사항: 검색 및 필터링 기능은 클라이언트 사이드에서만 동작

**연동 시 수정 필요 사항**:
- `menu/drink.html` 라인 640-687: 하드코딩된 메뉴 목록을 API로부터 받아 동적 렌더링
- 검색 및 필터 기능을 서버 사이드로 전환
- 페이지네이션 추가 필요

---

### 2.2 menu/drink_detail.html (담당: 🟢 이주희)

**화면 설명**: 메뉴 상세 페이지. 메뉴 정보, 영양 정보, 알레르기 정보, 옵션 선택 기능 포함.

**필요한 API**:

1. **GET /api/products/{menuCode}**
   - 용도: 특정 메뉴 상세 정보 조회 (메뉴 정보, 영양 정보, 알레르기, 레시피, 옵션 포함)
   - Path Variable: `menuCode`
   - Response:
```json
{
  "menuCode": 1,
  "menuName": "아메리카노",
  "category": "커피",
  "basePrice": 4500,
  "baseVolume": "Regular",
  "description": "진한 에스프레소에 뜨거운 물을 넣어 깔끔하고 강렬한 맛",
  "isAvailable": true,
  "allergyIds": "1,2",
  "allergies": [
    {"allergyId": 1, "allergyName": "우유"},
    {"allergyId": 2, "allergyName": "대두"}
  ],
  "nutrition": {
    "calories": 5,
    "sodium": 10,
    "carbs": 0,
    "sugars": 0,
    "protein": 0,
    "fat": 0,
    "saturatedFat": 0,
    "caffeine": 150
  },
  "options": {
    "샷선택": [
      {
        "optionId": 2,
        "optionName": "샷추가",
        "defaultPrice": 600,
        "optionGroupName": "샷선택"
      }
    ],
    "당도선택": [
      {
        "optionId": 6,
        "optionName": "바닐라시럽추가",
        "defaultPrice": 500,
        "optionGroupName": "당도선택"
      }
    ]
  }
}
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

2. **GET /api/products/{menuCode}/options**
   - 용도: 메뉴별 옵션 목록 조회 (옵션 그룹별)
   - Path Variable: `menuCode`
   - Response:
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
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ 하드코딩된 메뉴 정보
- ✅ localStorage 사용: 장바구니 추가 시 `localStorage.setItem('cart')`
- ⚠️ 주의사항: 옵션 선택 로직은 클라이언트 사이드에서 처리

**연동 시 수정 필요 사항**:
- `menu/drink_detail.html`: URL 파라미터에서 menuCode 추출 후 API 호출
- 메뉴 정보, 영양 정보, 알레르기 정보, 옵션 목록 동적 렌더링
- 장바구니 추가 시 서버 API 호출 (localStorage 대신 또는 함께)

---

## 3. 주문 관련

### 3.1 order/cart.html (담당: 🟡 박윤호)

**화면 설명**: 장바구니 페이지. 담은 상품 목록 조회, 수량 조절, 삭제, 선택 주문 기능 포함.

**필요한 API**:

1. **GET /api/cart**
   - 용도: 현재 사용자의 장바구니 조회 (cart_header → cart_item → cart_option 3단 구조)
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "cartId": 1,
  "customerId": "user123",
  "items": [
    {
      "cartItemId": 1,
      "menuCode": 1,
      "menuName": "아메리카노",
      "quantity": 2,
      "unitPrice": 4500,
      "options": [
        {
          "cartOptionId": 1,
          "optionId": 2,
          "optionName": "샷추가",
          "optionPrice": 600
        }
      ]
    }
  ],
  "totalAmount": 10200
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

2. **PUT /api/cart/items/{cartItemId}**
   - 용도: 장바구니 상품 수량 수정
   - Path Variable: `cartItemId`
   - Headers: `Authorization: Bearer {token}`
   - Request:
```json
{
  "quantity": 3
}
```
   - Response:
```json
{
  "message": "수량이 변경되었습니다.",
  "cartItemId": 1,
  "quantity": 3
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

3. **DELETE /api/cart/items/{cartItemId}**
   - 용도: 장바구니 상품 삭제
   - Path Variable: `cartItemId`
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "message": "상품이 삭제되었습니다."
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

4. **GET /api/cart/count**
   - 용도: 장바구니 상품 개수 조회 (헤더 배지 표시용)
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "count": 5
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('cart')` - 더미 장바구니 데이터
- ✅ 클라이언트 사이드에서 수량 조절 및 삭제 처리
- ⚠️ 주의사항: 로그인 사용자만 접근 가능해야 함

**연동 시 수정 필요 사항**:
- `order/cart.html` 라인 1002-1006: `loadCart()` 함수를 API 호출로 변경
- 라인 1085-1097: 수량 변경 시 API 호출
- 라인 삭제 버튼 클릭 시 API 호출
- 페이지 로드 시 JWT 토큰 확인 및 장바구니 조회

---

### 3.2 order/checkout.html (담당: 🟡 박윤호)

**화면 설명**: 주문 결제 페이지. 장바구니 상품 확인, 요청사항 입력, 주문 완료 처리.

**필요한 API**:

1. **GET /api/cart**
   - 용도: 주문 전 장바구니 최종 확인
   - Headers: `Authorization: Bearer {token}`
   - Response: (cart.html과 동일)
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

2. **POST /api/orders**
   - 용도: 주문 생성 (트랜잭션 필수: 재고 차감 → 주문 생성 → 장바구니 비우기)
   - Headers: `Authorization: Bearer {token}`
   - Request:
```json
{
  "request": "빨리 준비해주세요",
  "items": [
    {
      "cartItemId": 1,
      "menuCode": 1,
      "quantity": 2,
      "options": [2, 6]
    }
  ]
}
```
   - Response:
```json
{
  "orderId": 1,
  "message": "주문이 완료되었습니다.",
  "totalAmount": 10200,
  "orderDate": "2024-01-01T10:00:00"
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('cart')` - 장바구니 읽기
- ✅ localStorage 사용: `localStorage.setItem('orders')` - 주문 저장
- ✅ localStorage 사용: `localStorage.removeItem('cart')` - 장바구니 비우기
- ⚠️ 주의사항: 재고 차감 로직 없음, 트랜잭션 처리 없음

**연동 시 수정 필요 사항**:
- `order/checkout.html` 라인 1028-1067: 주문하기 버튼 클릭 시 API 호출
- 재고 부족 시 에러 처리
- 주문 성공 후 주문 상세 페이지로 이동

---

### 3.3 order/history.html (담당: 🟡 박윤호)

**화면 설명**: 주문 내역 목록 페이지. 본인의 주문 내역을 날짜순으로 조회, 상태별 필터링.

**필요한 API**:

1. **GET /api/orders**
   - 용도: 현재 사용자의 주문 목록 조회
   - Headers: `Authorization: Bearer {token}`
   - Query Parameters: `status` (선택), `startDate` (선택), `endDate` (선택), `page` (선택)
   - Response:
```json
[
  {
    "orderId": 1,
    "orderDate": "2024-01-01T10:00:00",
    "totalAmount": 10200,
    "status": "주문완료",
    "itemCount": 2
  }
]
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('orders')` - 더미 주문 데이터
- ⚠️ 주의사항: 본인 주문만 조회해야 함

**연동 시 수정 필요 사항**:
- `order/history.html`: 페이지 로드 시 API 호출
- 필터링 기능 서버 사이드로 전환
- 페이지네이션 추가

---

### 3.4 order/detail.html (담당: 🟡 박윤호)

**화면 설명**: 주문 상세 페이지. 주문 정보, 주문 상품 목록, 옵션 정보, 주문 상태 표시.

**필요한 API**:

1. **GET /api/orders/{orderId}**
   - 용도: 특정 주문 상세 정보 조회 (orders → order_item → order_option 3단 구조)
   - Path Variable: `orderId`
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "orderId": 1,
  "orderDate": "2024-01-01T10:00:00",
  "totalAmount": 10200,
  "status": "주문완료",
  "request": "빨리 준비해주세요",
  "customerId": "user123",
  "items": [
    {
      "orderItemId": 1,
      "menuCode": 1,
      "menuName": "아메리카노",
      "quantity": 2,
      "priceAtOrder": 4500,
      "totalItemPrice": 9000,
      "options": [
        {
          "orderOptionId": 1,
          "optionId": 2,
          "optionName": "샷추가",
          "optionPriceAtOrder": 600
        }
      ]
    }
  ]
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

2. **PUT /api/orders/{orderId}/cancel**
   - 용도: 주문 취소 (사용자용)
   - Path Variable: `orderId`
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "message": "주문이 취소되었습니다.",
  "orderId": 1,
  "status": "주문취소"
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('orders')` - 더미 주문 데이터
- ✅ URL 파라미터로 orderId 전달: `detail.html?orderId={orderId}`
- ⚠️ 주의사항: 본인 주문만 조회 가능해야 함

**연동 시 수정 필요 사항**:
- `order/detail.html`: URL 파라미터에서 orderId 추출 후 API 호출
- 주문 상세 정보 동적 렌더링
- 주문 취소 기능 추가 (상태에 따라 버튼 표시/숨김)

---

## 4. 마이페이지 관련

### 4.1 mypage/index.html (담당: 🔵 하성호)

**화면 설명**: 마이페이지 메인. 사용자 정보 요약, 주문 내역 링크, 문의 내역 링크 제공.

**필요한 API**:

1. **GET /api/auth/me**
   - 용도: 현재 로그인한 사용자 정보 조회
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "userId": "user123",
  "name": "홍길동",
  "email": "user@example.com",
  "userType": "member",
  "createdAt": "2024-01-01T10:00:00"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: `localStorage.getItem('userName')`, `localStorage.getItem('isLoggedIn')`
- ⚠️ 주의사항: 로그인 상태 확인 필요

**연동 시 수정 필요 사항**:
- `mypage/index.html`: 페이지 로드 시 API 호출하여 사용자 정보 표시
- JWT 토큰으로 인증 확인

---

### 4.2 mypage/edit.html (담당: 🔵 하성호)

**화면 설명**: 내 정보 수정 페이지. 이름, 이메일 등 사용자 정보 수정.

**필요한 API**:

1. **GET /api/auth/me**
   - 용도: 수정 전 현재 사용자 정보 조회
   - Headers: `Authorization: Bearer {token}`
   - Response: (mypage/index.html과 동일)
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **PUT /api/auth/me**
   - 용도: 사용자 정보 수정
   - Headers: `Authorization: Bearer {token}`
   - Request:
```json
{
  "name": "홍길동",
  "email": "newemail@example.com"
}
```
   - Response:
```json
{
  "message": "정보가 수정되었습니다.",
  "userId": "user123",
  "name": "홍길동",
  "email": "newemail@example.com"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 사용자 정보 읽기/쓰기
- ⚠️ 주의사항: 이메일 변경 시 재인증 필요할 수 있음

**연동 시 수정 필요 사항**:
- `mypage/edit.html`: 페이지 로드 시 API 호출하여 현재 정보 표시
- 폼 제출 시 API 호출하여 정보 업데이트

---

### 4.3 mypage/inquiry.html (담당: 🔵 하성호)

**화면 설명**: 문의 등록 페이지. 제목, 내용 입력하여 문의 등록.

**필요한 API**:

1. **POST /api/inquiries**
   - 용도: 문의 등록
   - Headers: `Authorization: Bearer {token}`
   - Request:
```json
{
  "title": "배송 문의",
  "content": "언제 배송되나요?"
}
```
   - Response:
```json
{
  "message": "문의가 등록되었습니다.",
  "inquiryId": 1
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 문의 저장
- ⚠️ 주의사항: 로그인 사용자만 등록 가능

**연동 시 수정 필요 사항**:
- `mypage/inquiry.html`: 폼 제출 시 API 호출
- 성공 시 문의 목록 페이지로 이동

---

### 4.4 mypage/inquiry_list.html (담당: 🔵 하성호)

**화면 설명**: 내 문의 목록 페이지. 본인이 등록한 문의 목록 조회, 상태별 필터링.

**필요한 API**:

1. **GET /api/inquiries/my**
   - 용도: 현재 사용자의 문의 목록 조회
   - Headers: `Authorization: Bearer {token}`
   - Query Parameters: `status` (선택: 'pending', 'answered'), `page` (선택)
   - Response:
```json
[
  {
    "inquiryId": 1,
    "title": "배송 문의",
    "status": "answered",
    "createdAt": "2024-01-01T10:00:00",
    "answer": "1-2일 소요됩니다."
  }
]
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 더미 문의 데이터
- ⚠️ 주의사항: 본인 문의만 조회

**연동 시 수정 필요 사항**:
- `mypage/inquiry_list.html`: 페이지 로드 시 API 호출
- 필터링 및 페이지네이션 추가

---

### 4.5 mypage/inquiry_detail.html (담당: 🔵 하성호)

**화면 설명**: 문의 상세 페이지. 문의 내용 및 답변 확인.

**필요한 API**:

1. **GET /api/inquiries/{inquiryId}**
   - 용도: 특정 문의 상세 정보 조회
   - Path Variable: `inquiryId`
   - Headers: `Authorization: Bearer {token}`
   - Response:
```json
{
  "inquiryId": 1,
  "title": "배송 문의",
  "content": "언제 배송되나요?",
  "status": "answered",
  "answer": "1-2일 소요됩니다.",
  "createdAt": "2024-01-01T10:00:00"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 더미 문의 데이터
- ⚠️ 주의사항: 본인 문의만 조회 가능

**연동 시 수정 필요 사항**:
- `mypage/inquiry_detail.html`: URL 파라미터에서 inquiryId 추출 후 API 호출
- 문의 상세 정보 동적 렌더링

---

## 5. 게시판 관련

### 5.1 bbs/notice.html (담당: 🔵 하성호)

**화면 설명**: 공지사항 목록 페이지. 공지사항 목록 조회, 검색, 페이지네이션, 상단 고정 공지 표시.

**필요한 API**:

1. **GET /api/notices**
   - 용도: 공지사항 목록 조회 (isPinned 우선 정렬)
   - Query Parameters: `search` (선택), `page` (선택), `size` (선택)
   - Response:
```json
[
  {
    "noticeId": 1,
    "title": "중요 공지사항",
    "authorId": "admin",
    "isPinned": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  {
    "noticeId": 2,
    "title": "일반 공지사항",
    "authorId": "admin",
    "isPinned": false,
    "createdAt": "2024-01-02T10:00:00"
  }
]
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ 하드코딩된 공지사항 목록
- ⚠️ 주의사항: isPinned=true인 공지는 상단에 고정 표시

**연동 시 수정 필요 사항**:
- `bbs/notice.html`: 페이지 로드 시 API 호출
- 검색 기능 서버 사이드로 전환
- 페이지네이션 추가
- isPinned 공지 상단 고정 처리

---

### 5.2 bbs/notice_detail.html (담당: 🔵 하성호)

**화면 설명**: 공지사항 상세 페이지. 공지사항 내용 확인.

**필요한 API**:

1. **GET /api/notices/{noticeId}**
   - 용도: 특정 공지사항 상세 정보 조회
   - Path Variable: `noticeId`
   - Response:
```json
{
  "noticeId": 1,
  "title": "중요 공지사항",
  "content": "공지사항 내용입니다.",
  "authorId": "admin",
  "isPinned": true,
  "createdAt": "2024-01-01T10:00:00"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ 하드코딩된 공지사항 상세 정보
- ⚠️ 주의사항: URL 파라미터로 noticeId 전달

**연동 시 수정 필요 사항**:
- `bbs/notice_detail.html`: URL 파라미터에서 noticeId 추출 후 API 호출
- 공지사항 상세 정보 동적 렌더링

---

### 5.3 bbs/event.html (담당: 🔵 하성호)

**화면 설명**: 이벤트 목록 페이지. 이벤트 공지사항 목록 조회.

**필요한 API**:

1. **GET /api/notices**
   - 용도: 이벤트 카테고리 공지사항 조회 (bbs_category=3)
   - Query Parameters: `bbs_category=3`, `page` (선택)
   - Response: (bbs/notice.html과 동일 형식)
   - 담당자: 🔵 하성호
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ 하드코딩된 이벤트 목록
- ⚠️ 주의사항: 공지사항과 동일한 구조일 수 있음

**연동 시 수정 필요 사항**:
- `bbs/event.html`: 카테고리 필터로 API 호출
- 페이지네이션 추가

---

### 5.4 bbs/faq.html (담당: 🔵 하성호)

**화면 설명**: FAQ 목록 페이지. 자주 묻는 질문 목록 조회.

**필요한 API**:

1. **GET /api/notices**
   - 용도: FAQ 카테고리 공지사항 조회 (bbs_category=4)
   - Query Parameters: `bbs_category=4`, `page` (선택)
   - Response: (bbs/notice.html과 동일 형식)
   - 담당자: 🔵 하성호
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ 하드코딩된 FAQ 목록
- ⚠️ 주의사항: 공지사항과 동일한 구조일 수 있음

**연동 시 수정 필요 사항**:
- `bbs/faq.html`: 카테고리 필터로 API 호출
- 페이지네이션 추가

---

## 6. 관리자 관련

### 6.1 admin/login.html (담당: 🔵 하성호)

**화면 설명**: 관리자 전용 로그인 페이지.

**필요한 API**:

1. **POST /api/admin/login**
   - 용도: 관리자 로그인
   - Request:
```json
{
  "userId": "admin",
  "password": "admin1234"
}
```
   - Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "Admin",
  "userId": "admin",
  "role": "ADMIN"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 더미 관리자 계정
- ⚠️ 주의사항: 관리자 권한 확인 필요

**연동 시 수정 필요 사항**:
- `admin/login.html`: 로그인 폼 제출 시 API 호출
- 성공 시 관리자 대시보드로 이동

---

### 6.2 admin/user.html (담당: 🔵 하성호)

**화면 설명**: 관리자 회원 관리 페이지. 회원 목록 조회, 검색, 수정, 삭제, 점주 전환 기능.

**필요한 API**:

1. **GET /api/admin/users**
   - 용도: 전체 회원 목록 조회
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Query Parameters: `search` (선택), `role` (선택: 'member', 'admin', 'store_owner'), `page` (선택)
   - Response:
```json
[
  {
    "userId": "user123",
    "name": "홍길동",
    "email": "user@example.com",
    "userType": "member",
    "createdAt": "2024-01-01T10:00:00"
  }
]
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **PUT /api/admin/users/{userId}**
   - 용도: 회원 정보 수정
   - Path Variable: `userId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Request:
```json
{
  "name": "홍길동",
  "email": "newemail@example.com",
  "userType": "member"
}
```
   - Response:
```json
{
  "message": "사용자 정보가 업데이트되었습니다.",
  "userId": "user123",
  "name": "홍길동",
  "email": "newemail@example.com",
  "userType": "member"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

3. **PUT /api/admin/users/{userId}/promote**
   - 용도: 회원을 점주로 전환
   - Path Variable: `userId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
{
  "message": "점주로 전환되었습니다.",
  "userId": "user123",
  "userType": "store_owner"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: MEDIUM

4. **DELETE /api/admin/users/{userId}**
   - 용도: 회원 삭제
   - Path Variable: `userId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
{
  "message": "사용자가 삭제되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 더미 회원 데이터
- ⚠️ 주의사항: ADMIN 권한 확인 필요

**연동 시 수정 필요 사항**:
- `admin/user.html`: 페이지 로드 시 API 호출
- 검색 및 필터 기능 서버 사이드로 전환
- 회원 수정/삭제/전환 기능 API 연동

---

### 6.3 admin/product.html (담당: 🟢 이주희)

**화면 설명**: 관리자 상품 관리 페이지. 메뉴 목록 조회, 검색, 등록, 수정, 삭제 기능.

**필요한 API**:

1. **GET /api/admin/products**
   - 용도: 관리자용 전체 상품 목록 조회
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Query Parameters: `search` (선택), `page` (선택)
   - Response: (menu/drink.html과 동일 형식)
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

2. **POST /api/admin/products**
   - 용도: 새 메뉴 등록 (트랜잭션: menu + nutrition + recipe + menu_option)
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Request:
```json
{
  "menuName": "아메리카노",
  "category": "커피",
  "basePrice": 4500,
  "baseVolume": "Regular",
  "description": "진한 에스프레소에 뜨거운 물을 넣어 깔끔하고 강렬한 맛",
  "createTime": 180,
  "isAvailable": true,
  "allergyIds": "1,2",
  "nutrition": {
    "calories": 5,
    "sodium": 10,
    "carbs": 0,
    "sugars": 0,
    "protein": 0,
    "fat": 0,
    "saturatedFat": 0,
    "caffeine": 150
  },
  "recipe": [
    {
      "ingredientId": 1,
      "requiredQuantity": 10.0,
      "unit": "g"
    }
  ],
  "menuOptions": [
    {
      "optionGroupName": "샷선택"
    }
  ]
}
```
   - Response:
```json
{
  "message": "메뉴가 등록되었습니다.",
  "menuCode": 1
}
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

3. **PUT /api/admin/products/{menuCode}**
   - 용도: 메뉴 정보 수정
   - Path Variable: `menuCode`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Request: (POST와 동일 형식)
   - Response:
```json
{
  "message": "메뉴가 수정되었습니다.",
  "menuCode": 1
}
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

4. **DELETE /api/admin/products/{menuCode}**
   - 용도: 메뉴 삭제
   - Path Variable: `menuCode`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
{
  "message": "메뉴가 삭제되었습니다."
}
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

5. **GET /api/admin/inventory**
   - 용도: 재료 목록 조회 (레시피 등록 시 재료 선택용)
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
[
  {
    "ingredientId": 1,
    "ingredientName": "커피원두",
    "baseUnit": "g",
    "stockQty": 1000.0
  }
]
```
   - 담당자: 🟢 이주희
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ 하드코딩된 상품 목록
- ✅ 폼 제출 핸들러 존재 (라인 1547-1560)
- ⚠️ 주의사항: ADMIN 권한 확인, 트랜잭션 처리 필요

**연동 시 수정 필요 사항**:
- `admin/product.html`: 페이지 로드 시 API 호출
- 메뉴 등록/수정 폼 제출 시 API 호출 (트랜잭션 처리)
- 메뉴 삭제 기능 API 연동
- 재료 목록 API 연동 (레시피 등록 시)

---

### 6.4 admin/notice.html (담당: 🔵 하성호)

**화면 설명**: 관리자 공지사항 관리 페이지. 공지사항 목록, 등록, 수정, 삭제, 상단 고정 기능.

**필요한 API**:

1. **GET /api/admin/notices**
   - 용도: 관리자용 공지사항 목록 조회 (검색 필터 포함)
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Query Parameters: `search` (선택), `page` (선택)
   - Response: (bbs/notice.html과 동일 형식)
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **POST /api/admin/notices**
   - 용도: 공지사항 등록
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Request:
```json
{
  "title": "중요 공지사항",
  "content": "공지사항 내용입니다.",
  "isPinned": true
}
```
   - Response:
```json
{
  "message": "공지사항이 등록되었습니다.",
  "noticeId": 1
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

3. **PUT /api/admin/notices/{noticeId}**
   - 용도: 공지사항 수정
   - Path Variable: `noticeId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Request: (POST와 동일 형식)
   - Response:
```json
{
  "message": "공지사항이 수정되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

4. **DELETE /api/admin/notices/{noticeId}**
   - 용도: 공지사항 삭제
   - Path Variable: `noticeId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
{
  "message": "공지사항이 삭제되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

5. **POST /api/admin/notices/{noticeId}/pin**
   - 용도: 공지사항 상단 고정 토글
   - Path Variable: `noticeId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
{
  "message": "공지사항이 상단에 고정되었습니다.",
  "isPinned": true
}
```
   - 담당자: 🔵 하성호
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ 하드코딩된 공지사항 목록
- ✅ 폼 제출 핸들러 존재 (라인 1044-1055)
- ⚠️ 주의사항: ADMIN 권한 확인 필요

**연동 시 수정 필요 사항**:
- `admin/notice.html`: 페이지 로드 시 API 호출
- 공지사항 등록/수정/삭제 기능 API 연동
- 상단 고정 기능 API 연동

---

### 6.5 admin/inquiry.html (담당: 🔵 하성호)

**화면 설명**: 관리자 문의 관리 페이지. 전체 문의 목록 조회, 검색, 답변 작성, 삭제 기능.

**필요한 API**:

1. **GET /api/admin/inquiries**
   - 용도: 관리자용 문의 목록 조회 (검색, 상태 필터 포함)
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Query Parameters: `search` (선택), `status` (선택: 'pending', 'answered'), `page` (선택)
   - Response:
```json
[
  {
    "inquiryId": 1,
    "userId": "user123",
    "title": "배송 문의",
    "status": "pending",
    "createdAt": "2024-01-01T10:00:00"
  }
]
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

2. **PUT /api/admin/inquiries/{inquiryId}/answer**
   - 용도: 문의 답변 작성
   - Path Variable: `inquiryId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Request:
```json
{
  "answer": "1-2일 소요됩니다."
}
```
   - Response:
```json
{
  "message": "답변이 작성되었습니다.",
  "inquiryId": 1,
  "status": "answered"
}
```
   - 담당자: 🔵 하성호
   - 우선순위: HIGH

3. **DELETE /api/admin/inquiries/{inquiryId}**
   - 용도: 문의 삭제
   - Path Variable: `inquiryId`
   - Headers: `Authorization: Bearer {token}` (ADMIN 권한)
   - Response:
```json
{
  "message": "문의가 삭제되었습니다."
}
```
   - 담당자: 🔵 하성호
   - 우선순위: MEDIUM

**현재 구현 상태**:
- ✅ 하드코딩된 문의 목록
- ⚠️ 주의사항: ADMIN 권한 확인 필요

**연동 시 수정 필요 사항**:
- `admin/inquiry.html`: 페이지 로드 시 API 호출
- 문의 답변 기능 API 연동
- 문의 삭제 기능 API 연동

---

## 7. 점주 관련

### 7.1 owner/order.html (담당: 🟡 박윤호)

**화면 설명**: 점주 주문 관리 페이지. 전체 주문 목록 조회, 상태 필터링, 주문 상태 변경 기능.

**필요한 API**:

1. **GET /api/owner/orders**
   - 용도: 점주용 주문 목록 조회 (모든 주문, 상태 필터 포함)
   - Headers: `Authorization: Bearer {token}` (OWNER 권한)
   - Query Parameters: `status` (선택), `page` (선택)
   - Response:
```json
[
  {
    "orderId": 1,
    "orderDate": "2024-01-01T10:00:00",
    "totalAmount": 10200,
    "status": "주문완료",
    "customerId": "user123",
    "customerName": "홍길동",
    "itemCount": 2
  }
]
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

2. **GET /api/owner/orders/stats**
   - 용도: 주문 통계 조회 (점주 대시보드용)
   - Headers: `Authorization: Bearer {token}` (OWNER 권한)
   - Response:
```json
{
  "todayOrders": 10,
  "pendingOrders": 5,
  "todayRevenue": 150000
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: MEDIUM

3. **PUT /api/owner/orders/{orderId}/status**
   - 용도: 주문 상태 변경 (접수/완료/픽업/취소), 취소 시 재고 복구
   - Path Variable: `orderId`
   - Headers: `Authorization: Bearer {token}` (OWNER 권한)
   - Request:
```json
{
  "status": "접수완료"
}
```
   - Response:
```json
{
  "message": "주문 상태가 변경되었습니다.",
  "orderId": 1,
  "status": "접수완료"
}
```
   - 담당자: 🟡 박윤호
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ localStorage 사용: 더미 주문 데이터
- ⚠️ 주의사항: OWNER 권한 확인 필요, 취소 시 재고 복구 로직 필요

**연동 시 수정 필요 사항**:
- `owner/order.html`: 페이지 로드 시 API 호출
- 주문 상태 변경 기능 API 연동
- 통계 정보 표시 (선택)

---

### 7.2 owner/inventory.html (담당: 🟢 이주희)

**화면 설명**: 점주 재고 관리 페이지. 재료 목록 조회, 재고 수량 조회, 재고 추가/수정 기능.

**필요한 API**:

1. **GET /api/owner/inventory**
   - 용도: 재료 재고 목록 조회
   - Headers: `Authorization: Bearer {token}` (OWNER 권한)
   - Response:
```json
[
  {
    "ingredientId": 1,
    "ingredientName": "커피원두",
    "baseUnit": "g",
    "stockQty": 1000.0
  }
]
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

2. **PUT /api/owner/inventory/{ingredientId}/add**
   - 용도: 재고 추가 (수량 증가)
   - Path Variable: `ingredientId`
   - Headers: `Authorization: Bearer {token}` (OWNER 권한)
   - Request:
```json
{
  "quantity": 100.0
}
```
   - Response:
```json
{
  "message": "재고가 추가되었습니다.",
  "ingredientId": 1,
  "stockQty": 1100.0
}
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

3. **PUT /api/owner/inventory/{ingredientId}**
   - 용도: 재고 수정 (수량 직접 설정)
   - Path Variable: `ingredientId`
   - Headers: `Authorization: Bearer {token}` (OWNER 권한)
   - Request:
```json
{
  "stockQty": 1500.0
}
```
   - Response:
```json
{
  "message": "재고가 수정되었습니다.",
  "ingredientId": 1,
  "stockQty": 1500.0
}
```
   - 담당자: 🟢 이주희
   - 우선순위: HIGH

**현재 구현 상태**:
- ✅ 하드코딩된 재고 목록
- ⚠️ 주의사항: OWNER 권한 확인 필요

**연동 시 수정 필요 사항**:
- `owner/inventory.html`: 페이지 로드 시 API 호출
- 재고 추가/수정 기능 API 연동

---

## 8. 기타

### 8.1 index.html

**화면 설명**: 메인 홈 페이지. 브랜드 소개, 메뉴 소개, 이벤트 목록 등 표시.

**필요한 API**:

1. **GET /api/products**
   - 용도: 추천 메뉴 목록 조회 (홈페이지 표시용)
   - Query Parameters: `limit` (선택: 예: 4개)
   - Response: (menu/drink.html과 동일 형식)
   - 담당자: 🟢 이주희
   - 우선순위: MEDIUM

2. **GET /api/notices**
   - 용도: 최신 이벤트 공지사항 조회 (홈페이지 표시용)
   - Query Parameters: `bbs_category=3`, `limit` (선택: 예: 4개)
   - Response: (bbs/notice.html과 동일 형식)
   - 담당자: 🔵 하성호
   - 우선순위: LOW

**현재 구현 상태**:
- ✅ 하드코딩된 콘텐츠
- ⚠️ 주의사항: 정적 페이지로도 충분할 수 있음

**연동 시 수정 필요 사항**:
- `index.html`: 선택적으로 API 호출하여 동적 콘텐츠 표시

---

### 8.2 about/brand.html, about/bi.html, about/map.html

**화면 설명**: 회사 소개, BI 정보, 매장 위치 정보 페이지.

**필요한 API**: 없음 (정적 페이지)

**현재 구현 상태**:
- ✅ 정적 콘텐츠만 존재

**연동 시 수정 필요 사항**: 없음

---

### 8.3 policy/privacy_policy.html, policy/service_policy.html

**화면 설명**: 개인정보처리방침, 이용약관 페이지.

**필요한 API**: 없음 (정적 페이지)

**현재 구현 상태**:
- ✅ 정적 콘텐츠만 존재

**연동 시 수정 필요 사항**: 없음

---

## 요약

### 담당자별 API 수

- 🔵 **하성호**: 20개 API (인증 8개, 게시판 4개, 문의 5개, 관리자 3개)
- 🟢 **이주희**: 17개 API (메뉴 5개, 재고 5개, 관리자 상품 5개, 점주 재고 2개)
- 🟡 **박윤호**: 13개 API (장바구니 4개, 주문 4개, 점주 주문 3개, 기타 2개)

### 우선순위별 분류

- **HIGH**: 필수 구현 API (40개)
- **MEDIUM**: 선택적 구현 API (8개)
- **LOW**: 부가 기능 API (2개)

### 주요 변경 사항

1. **localStorage 제거**: 모든 localStorage 기반 더미 데이터를 실제 API 호출로 변경
2. **JWT 인증**: 로그인 필요한 모든 페이지에 JWT 토큰 인증 추가
3. **권한 확인**: ADMIN/OWNER 권한이 필요한 API에 권한 검증 추가
4. **에러 처리**: API 호출 실패 시 적절한 에러 메시지 표시
5. **로딩 상태**: API 호출 중 로딩 인디케이터 표시

---

**문서 작성일**: 2024년  
**최종 수정일**: 2024년

