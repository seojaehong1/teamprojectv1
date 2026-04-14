# API Specification Document

This document provides a comprehensive specification of all REST API endpoints across the microservices architecture.

## Table of Contents
1. [Admin Service](#admin-service)
2. [Board Service](#board-service)
3. [Frontend Service](#frontend-service)
4. [Inventory Service](#inventory-service)
5. [Member Service](#member-service)
6. [Order Service](#order-service)
7. [Product Service](#product-service)

---

## Admin Service

Service responsible for administrative functions including authentication, user management, inquiry handling, notice management, and product management.

### AdminAuthController

#### Login (Admin)
- **Endpoint**: `POST /api/admin/login`
- **Authentication**: None (public)
- **Request Body**:
  ```json
  {
    "userId": "string",
    "password": "string"
  }
  ```
- **Response**:
  ```json
  {
    "accessToken": "string",
    "refreshToken": "string",
    "userId": "string",
    "userName": "string",
    "userRole": "ADMIN"
  }
  ```
- **Description**: Admin/Store Owner login endpoint. Only users with 'admin' or 'store_owner' roles can access.

---

### AdminInquiryController

All endpoints require admin or store_owner role (validated via request attributes).

#### Get Inquiry List
- **Endpoint**: `GET /api/admin/inquiries`
- **Authentication**: Required (Admin/Store Owner)
- **Query Parameters**:
  - `page` (optional, default: 0): Page number
  - `size` (optional, default: 10, max: 50): Page size
  - `keyword` (optional): Search keyword (URL encoded, searches in title/content)
  - `status` (optional): Filter by status (PENDING, ANSWERED)
- **Response**:
  ```json
  {
    "inquiries": [
      {
        "inquiryId": "number",
        "title": "string",
        "content": "string",
        "userId": "string",
        "status": "string",
        "answer": "string (nullable)",
        "answeredBy": "string (nullable)",
        "answeredAt": "datetime (nullable)",
        "createdAt": "datetime"
      }
    ],
    "currentPage": "number",
    "totalPages": "number",
    "totalItems": "number"
  }
  ```

#### Get Inquiry Detail
- **Endpoint**: `GET /api/admin/inquiries/{id}`
- **Authentication**: Required (Admin/Store Owner)
- **Path Variables**: `id` - Inquiry ID
- **Response**: Single Inquiry object

#### Answer Inquiry
- **Endpoint**: `POST /api/admin/inquiries/{id}/answer`
- **Authentication**: Required (Admin/Store Owner)
- **Path Variables**: `id` - Inquiry ID
- **Request Body**:
  ```json
  {
    "answer": "string (10-2000 characters)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "답변이 등록되었습니다."
  }
  ```

#### Delete Inquiry
- **Endpoint**: `DELETE /api/admin/inquiries/{id}`
- **Authentication**: Required (Admin/Store Owner)
- **Path Variables**: `id` - Inquiry ID
- **Response**:
  ```json
  {
    "message": "문의가 삭제되었습니다."
  }
  ```

#### Update Inquiry Status
- **Endpoint**: `PATCH /api/admin/inquiries/{id}/status`
- **Authentication**: Required (Admin/Store Owner)
- **Path Variables**: `id` - Inquiry ID
- **Request Body**:
  ```json
  {
    "status": "string (PENDING|ANSWERED)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "문의 상태가 변경되었습니다."
  }
  ```

---

### AdminNoticeController

Proxies requests to board-service with admin headers.

#### Get Notice List
- **Endpoint**: `GET /api/admin/notices`
- **Authentication**: None (proxied)
- **Query Parameters**:
  - `page` (optional, default: 1): Page number
  - `size` (optional, default: 10): Page size
  - `keyword` (optional): Search keyword (URL encoded)
  - `searchType` (optional, default: "title"): Search type
- **Response**: Proxied from board-service

#### Get Notice Detail
- **Endpoint**: `GET /api/admin/notices/{id}`
- **Authentication**: None (proxied)
- **Path Variables**: `id` - Notice ID
- **Response**: Proxied from board-service

#### Create Notice
- **Endpoint**: `POST /api/admin/notices`
- **Authentication**: None (proxied with X-User-Role: ADMIN header)
- **Request Body**:
  ```json
  {
    "title": "string",
    "content": "string",
    "isPinned": "boolean (optional)"
  }
  ```
- **Response**: Proxied from board-service

#### Update Notice
- **Endpoint**: `PUT /api/admin/notices/{id}`
- **Authentication**: None (proxied with X-User-Role: ADMIN header)
- **Path Variables**: `id` - Notice ID
- **Request Body**:
  ```json
  {
    "title": "string",
    "content": "string",
    "isPinned": "boolean (optional)"
  }
  ```
- **Response**: Proxied from board-service

#### Delete Notice
- **Endpoint**: `DELETE /api/admin/notices/{id}`
- **Authentication**: None (proxied with X-User-Role: ADMIN header)
- **Path Variables**: `id` - Notice ID
- **Response**: Proxied from board-service

#### Toggle Pin
- **Endpoint**: `PATCH /api/admin/notices/{id}/pin`
- **Authentication**: None (proxied with X-User-Role: ADMIN header)
- **Path Variables**: `id` - Notice ID
- **Response**: Proxied from board-service

---

### AdminProductController

#### Get Product List
- **Endpoint**: `GET /api/admin/products`
- **Authentication**: None
- **Query Parameters**:
  - `page` (optional, default: 0): Page number
  - `size` (optional, default: 10, max: 50): Page size
  - `keyword` (optional): Search keyword (URL encoded, searches in menuName)
  - `category` (optional): Filter by category
- **Response**:
  ```json
  {
    "products": [
      {
        "menuCode": "string (PK)",
        "menuName": "string",
        "basePrice": "decimal",
        "category": "string",
        "description": "string (nullable)",
        "imageUrl": "string (nullable)",
        "isAvailable": "boolean",
        "createdAt": "datetime"
      }
    ],
    "currentPage": "number",
    "totalPages": "number",
    "totalItems": "number"
  }
  ```

#### Get Product by Code
- **Endpoint**: `GET /api/admin/products/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Product code
- **Response**: Single Product object

#### Create Product
- **Endpoint**: `POST /api/admin/products`
- **Authentication**: None
- **Request Parameters** (multipart/form-data):
  - `menuCode`: string (required)
  - `menuName`: string (required, 2-100 characters)
  - `basePrice`: decimal (required, > 0)
  - `category`: string (required)
  - `description`: string (optional, max 500 characters)
  - `image`: file (optional, max 5MB, jpg/png/webp)
- **Response**:
  ```json
  {
    "message": "상품이 등록되었습니다.",
    "menuCode": "string"
  }
  ```

#### Update Product
- **Endpoint**: `PUT /api/admin/products/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Product code
- **Request Parameters** (multipart/form-data):
  - `menuName`: string (optional, 2-100 characters)
  - `basePrice`: decimal (optional, > 0)
  - `category`: string (optional)
  - `description`: string (optional, max 500 characters)
  - `isAvailable`: boolean (optional)
  - `image`: file (optional, max 5MB, jpg/png/webp)
- **Response**:
  ```json
  {
    "message": "상품이 수정되었습니다."
  }
  ```

#### Delete Product
- **Endpoint**: `DELETE /api/admin/products/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Product code
- **Response**:
  ```json
  {
    "message": "상품이 삭제되었습니다."
  }
  ```

---

### AdminUserController

#### Get User List
- **Endpoint**: `GET /api/admin/users`
- **Authentication**: None
- **Query Parameters**:
  - `page` (optional, default: 0): Page number
  - `size` (optional, default: 10, max: 50): Page size
  - `keyword` (optional): Search keyword (URL encoded, searches in name/userId/email)
  - `userType` (optional): Filter by user type (admin, member, store_owner)
- **Response**:
  ```json
  {
    "users": [
      {
        "userId": "string",
        "name": "string",
        "email": "string",
        "userType": "string",
        "createdAt": "datetime"
      }
    ],
    "currentPage": "number",
    "totalPages": "number",
    "totalItems": "number"
  }
  ```
- **Note**: Admin users are sorted first

#### Get User Detail
- **Endpoint**: `GET /api/admin/users/{userId}`
- **Authentication**: None
- **Path Variables**: `userId` - User ID
- **Response**: Single User object

#### Update User
- **Endpoint**: `PUT /api/admin/users/{userId}`
- **Authentication**: None
- **Path Variables**: `userId` - User ID
- **Request Body**:
  ```json
  {
    "name": "string (optional, 2-20 characters)",
    "email": "string (optional, valid email format)",
    "password": "string (optional)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "회원 정보가 수정되었습니다."
  }
  ```

#### Change User Role
- **Endpoint**: `PATCH /api/admin/users/{userId}/role`
- **Authentication**: None
- **Path Variables**: `userId` - User ID
- **Request Body**:
  ```json
  {
    "userType": "string (member|admin|store_owner)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "회원 권한이 변경되었습니다."
  }
  ```

#### Delete User
- **Endpoint**: `DELETE /api/admin/users/{userId}`
- **Authentication**: None
- **Path Variables**: `userId` - User ID
- **Response**:
  ```json
  {
    "message": "회원이 삭제되었습니다."
  }
  ```
- **Note**: Cannot delete admin accounts

---

### InquiryController (User)

#### Create Inquiry
- **Endpoint**: `POST /api/inquiries`
- **Authentication**: Required (JWT via request attributes)
- **Request Body**:
  ```json
  {
    "title": "string (max 200 characters)",
    "content": "string (max 5000 characters)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "문의가 등록되었습니다.",
    "inquiryId": "number"
  }
  ```

#### Get My Inquiries
- **Endpoint**: `GET /api/inquiries/my`
- **Authentication**: Required (JWT via request attributes)
- **Query Parameters**:
  - `page` (optional, default: 0): Page number
  - `size` (optional, default: 10, max: 50): Page size
- **Response**:
  ```json
  {
    "inquiries": [/* Inquiry objects */],
    "currentPage": "number",
    "totalPages": "number",
    "totalItems": "number"
  }
  ```

#### Get My Inquiry Detail
- **Endpoint**: `GET /api/inquiries/{id}`
- **Authentication**: Required (JWT via request attributes)
- **Path Variables**: `id` - Inquiry ID
- **Response**: Single Inquiry object
- **Note**: Users can only view their own inquiries

---

## Board Service

Service responsible for community boards, notices, and comments.

### BoardController

#### Get All Boards
- **Endpoint**: `GET /api/boards`
- **Authentication**: None
- **Response**: Array of Board objects
  ```json
  [
    {
      "inquiryId": "number",
      "title": "string",
      "content": "string",
      "userId": "string",
      "createdAt": "datetime",
      "updatedAt": "datetime"
    }
  ]
  ```

#### Get Board Detail
- **Endpoint**: `GET /api/boards/{id}`
- **Authentication**: None
- **Path Variables**: `id` - Board ID
- **Response**: Single Board object

#### Create Board
- **Endpoint**: `POST /api/boards`
- **Authentication**: Required (JWT via request attributes)
- **Request Body**:
  ```json
  {
    "title": "string",
    "content": "string"
  }
  ```
- **Response**:
  ```json
  {
    "message": "게시글이 작성되었습니다.",
    "boardId": "number"
  }
  ```

#### Update Board
- **Endpoint**: `PUT /api/boards/{id}`
- **Authentication**: Required (JWT via request attributes)
- **Path Variables**: `id` - Board ID
- **Request Body**:
  ```json
  {
    "title": "string",
    "content": "string"
  }
  ```
- **Response**:
  ```json
  {
    "message": "게시글이 수정되었습니다."
  }
  ```
- **Note**: Only the author can update

#### Delete Board
- **Endpoint**: `DELETE /api/boards/{id}`
- **Authentication**: Required (JWT via request attributes)
- **Path Variables**: `id` - Board ID
- **Response**:
  ```json
  {
    "message": "게시글이 삭제되었습니다."
  }
  ```
- **Note**: Only the author can delete

#### Get Boards by User
- **Endpoint**: `GET /api/boards/user/{userId}`
- **Authentication**: None
- **Path Variables**: `userId` - User ID
- **Response**: Array of Board objects

---

### CommentController

#### Get Comments by Board
- **Endpoint**: `GET /api/comments/board/{boardId}`
- **Authentication**: None
- **Path Variables**: `boardId` - Board ID
- **Response**: Array of Comment objects
  ```json
  [
    {
      "id": "number",
      "boardId": "number",
      "content": "string",
      "author": "string",
      "createdAt": "datetime"
    }
  ]
  ```

#### Create Comment
- **Endpoint**: `POST /api/comments`
- **Authentication**: Required (JWT via request attributes)
- **Request Body**:
  ```json
  {
    "boardId": "number",
    "content": "string"
  }
  ```
- **Response**:
  ```json
  {
    "message": "댓글이 작성되었습니다.",
    "commentId": "number"
  }
  ```

#### Delete Comment
- **Endpoint**: `DELETE /api/comments/{id}`
- **Authentication**: Required (JWT via Authorization header)
- **Path Variables**: `id` - Comment ID
- **Response**:
  ```json
  {
    "message": "댓글이 삭제되었습니다."
  }
  ```
- **Note**: Admin or author can delete

#### Get Comments by Author
- **Endpoint**: `GET /api/comments/author/{author}`
- **Authentication**: None
- **Path Variables**: `author` - Author ID
- **Response**: Array of Comment objects

---

### EventController

#### Event Page
- **Endpoint**: `GET /bbs/event`
- **Authentication**: None
- **Response**: HTML page (template: bbs/event)

---

### NoticeController

#### Get All Notices
- **Endpoint**: `GET /api/notices`
- **Authentication**: None
- **Query Parameters**:
  - `page` (optional, default: 1): Page number (1-based)
  - `limit` (optional, default: 10): Page size
  - `keyword` (optional): Search keyword (URL encoded)
  - `searchType` (optional, default: "title"): Search type (title|content|author)
- **Response**:
  ```json
  {
    "notices": [
      {
        "noticeId": "number",
        "title": "string",
        "content": "string",
        "author": "string",
        "isPinned": "boolean",
        "createdAt": "datetime"
      }
    ],
    "currentPage": "number",
    "totalPages": "number",
    "totalItems": "number"
  }
  ```
- **Note**: Pinned notices appear first, then sorted by creation date

#### Get Notice Detail
- **Endpoint**: `GET /api/notices/{id}`
- **Authentication**: None
- **Path Variables**: `id` - Notice ID
- **Response**: Single Notice object

#### Create Notice (Admin)
- **Endpoint**: `POST /api/notices/admin`
- **Authentication**: Required (ADMIN role via request attributes or X-User-Role header)
- **Request Body**:
  ```json
  {
    "title": "string (max 200 characters)",
    "content": "string (max 10000 characters)",
    "isImportant": "boolean (optional)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "공지사항이 등록되었습니다.",
    "noticeId": "number"
  }
  ```

#### Update Notice (Admin)
- **Endpoint**: `PUT /api/notices/admin/{id}`
- **Authentication**: Required (ADMIN role)
- **Path Variables**: `id` - Notice ID
- **Request Body**:
  ```json
  {
    "title": "string",
    "content": "string",
    "isImportant": "boolean"
  }
  ```
- **Response**:
  ```json
  {
    "message": "공지사항이 수정되었습니다."
  }
  ```

#### Delete Notice (Admin)
- **Endpoint**: `DELETE /api/notices/admin/{id}`
- **Authentication**: Required (ADMIN role)
- **Path Variables**: `id` - Notice ID
- **Response**:
  ```json
  {
    "message": "공지사항이 삭제되었습니다."
  }
  ```

#### Toggle Pin (Admin)
- **Endpoint**: `PATCH /api/notices/admin/{id}/toggle-pin`
- **Authentication**: Required (ADMIN role)
- **Path Variables**: `id` - Notice ID
- **Response**:
  ```json
  {
    "message": "공지가 고정되었습니다.|공지 고정이 해제되었습니다.",
    "isPinned": "boolean"
  }
  ```

---

## Frontend Service

Service responsible for serving frontend pages and proxying API requests to backend microservices.

### AdminController (Frontend)

These endpoints serve HTML pages (not REST APIs).

- `GET /admin/products` - Product management page
- `GET /admin/users` - User management page
- `GET /admin/notices` - Notice management page
- `GET /admin/inquiries` - Inquiry management page

---

### ApiProxyController

This controller proxies all API requests to appropriate microservices. All endpoints preserve original request paths, query parameters, headers, and request bodies.

#### Member Service Proxies
- `ANY /api/auth/**` → member-service
- `ANY /api/users/**` → member-service
- `ANY /api/admin/users/**` → member-service

#### Board Service Proxies
- `ANY /api/notices/**` → board-service
- `ANY /api/boards/**` → board-service
- `ANY /api/comments/**` → board-service

#### Admin Service Proxies
- `ANY /api/admin/notices/**` → admin-service
- `ANY /api/admin/inquiries/**` → admin-service
- `ANY /api/inquiries/**` → admin-service

#### Product Service Proxies
- `ANY /api/products/**` → product-service

#### Order Service Proxies
- `ANY /api/orders/**` → order-service

#### Inventory Service Proxies
- `ANY /api/inventory/**` → inventory-service

**Note**: Proxies support all HTTP methods (GET, POST, PUT, DELETE, PATCH). Request/response formats depend on the target service endpoint.

---

### AuthController (Frontend)

These endpoints serve HTML pages (not REST APIs).

- `GET /login` - Login page
- `GET /signup` - Signup page
- `GET /find_id` - Find ID page
- `GET /find_password` - Find password page

---

### MainController (Frontend)

These endpoints serve HTML pages (not REST APIs).

- `GET /` - Home page
- `GET /about/brand` - Brand page
- `GET /about/bi` - BI page
- `GET /about/map` - Map page
- `GET /bbs/notice` - Notice list page
- `GET /bbs/notice_detail` - Notice detail page
- `GET /bbs/faq` - FAQ page
- `GET /bbs/faq_detail` - FAQ detail page
- `GET /bbs/event` - Event page
- `GET /bbs/event_detail` - Event detail page
- `GET /mypage/index` - My page
- `GET /mypage/edit` - Profile edit page
- `GET /mypage/inquiry` - Inquiry page
- `GET /mypage/inquiry_detail` - Inquiry detail page
- `GET /mypage/inquiry_list` - Inquiry list page
- `GET /admin/user` - Admin user page
- `GET /admin/product` - Admin product page
- `GET /admin/notice` - Admin notice page
- `GET /admin/inquiry` - Admin inquiry page
- `GET /policy/privacy_policy` - Privacy policy page
- `GET /policy/service_policy` - Service policy page

---

### OrderController (Frontend)

These endpoints serve HTML pages (not REST APIs).

- `GET /order/cart` - Cart page (loads cart data from backend)
- `GET /order/checkout` - Checkout page
- `GET /order/detail` - Order detail page
- `GET /order/history` - Order history page

---

### OwnerController (Frontend)

These endpoints serve HTML pages (not REST APIs).

- `GET /owner/order` - Owner order page
- `GET /owner/inventory` - Owner inventory page

---

### ProductController (Frontend)

These endpoints serve HTML pages (not REST APIs).

- `GET /menu/drink` - Drink menu list page
- `GET /menu/drink-detail?menuCode={menuCode}` - Drink detail page

---

## Inventory Service

Service responsible for inventory management and stock processing.

### InventoryController

#### Process Order Stock
- **Endpoint**: `POST /api/inventory/process`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "orderId": "number",
    "menuCode": "number",
    "quantity": "number",
    "options": [
      {
        "optionId": "number",
        "quantity": "number"
      }
    ]
  }
  ```
- **Response**:
  ```json
  {
    "success": "boolean",
    "message": "string",
    "orderId": "number"
  }
  ```

#### List Options
- **Endpoint**: `GET /api/inventory/options`
- **Authentication**: None
- **Response**: Array of OptionMaster objects
  ```json
  [
    {
      "optionId": "number",
      "optionName": "string",
      "optionGroupName": "string",
      "optionPrice": "number",
      "ingredientName": "string (nullable)",
      "requiredQty": "decimal (nullable)",
      "baseUnit": "string (nullable)",
      "actionType": "string (nullable)"
    }
  ]
  ```

---

### MaterialController

#### List Materials Page
- **Endpoint**: `GET /materials`
- **Authentication**: None
- **Response**: HTML page with materials list

---

### OwnerController (Inventory)

#### Get Inventory List
- **Endpoint**: `GET /api/owner/inventory`
- **Authentication**: None
- **Response**:
  ```json
  [
    {
      "ingredientId": "number",
      "ingredientName": "string",
      "baseUnit": "string",
      "stockQty": "decimal"
    }
  ]
  ```

#### Create Inventory
- **Endpoint**: `POST /api/owner/inventory`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "ingredientName": "string",
    "stockQty": "decimal",
    "baseUnit": "string"
  }
  ```
- **Response**:
  ```json
  {
    "ingredientId": "number",
    "ingredientName": "string",
    "baseUnit": "string",
    "stockQty": "decimal"
  }
  ```

#### Update Stock
- **Endpoint**: `PUT /api/owner/inventory/{ingredientId}`
- **Authentication**: None
- **Path Variables**: `ingredientId` - Ingredient ID
- **Request Body**:
  ```json
  {
    "stockQty": "decimal"
  }
  ```
- **Response**:
  ```json
  {
    "message": "string",
    "ingredientId": "number",
    "stockQty": "decimal"
  }
  ```

---

## Member Service

Service responsible for member authentication and user management.

### AdminController (Member)

All endpoints require Bearer token authentication with admin or store_owner role.

#### Get All Users
- **Endpoint**: `GET /api/admin/users`
- **Authentication**: Required (Bearer token, Admin/Store Owner role)
- **Query Parameters**:
  - `keyword` (optional): Search keyword (searches name, userId, email)
  - `userType` (optional): Filter by user type (admin|member|store_owner)
- **Response**:
  ```json
  [
    {
      "id": "string",
      "userId": "string",
      "username": "string",
      "name": "string",
      "email": "string",
      "role": "string",
      "userType": "string",
      "createdAt": "string"
    }
  ]
  ```
- **Note**: Admin users are sorted first

#### Get User by ID
- **Endpoint**: `GET /api/admin/users/{id}`
- **Authentication**: Required (Bearer token, Admin/Store Owner role)
- **Path Variables**: `id` - User ID
- **Response**: Single user object

#### Update User
- **Endpoint**: `PUT /api/admin/users/{id}`
- **Authentication**: Required (Bearer token, Admin/Store Owner role)
- **Path Variables**: `id` - User ID
- **Request Body**:
  ```json
  {
    "name": "string (optional)",
    "username": "string (optional, alias for name)",
    "email": "string (optional)",
    "role": "string (optional)",
    "password": "string (optional)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "사용자 정보가 업데이트되었습니다.",
    "id": "string",
    "username": "string",
    "email": "string",
    "role": "string"
  }
  ```

#### Delete User
- **Endpoint**: `DELETE /api/admin/users/{id}`
- **Authentication**: Required (Bearer token, Admin/Store Owner role)
- **Path Variables**: `id` - User ID
- **Response**:
  ```json
  {
    "message": "사용자가 삭제되었습니다."
  }
  ```

#### Update User Role
- **Endpoint**: `PATCH /api/admin/users/{id}/role`
- **Authentication**: Required (Bearer token, Admin/Store Owner role)
- **Path Variables**: `id` - User ID
- **Request Body**:
  ```json
  {
    "userType": "string (member|admin|store_owner)",
    "role": "string (alias for userType)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "권한이 변경되었습니다.",
    "id": "string",
    "username": "string",
    "email": "string",
    "role": "string",
    "userType": "string"
  }
  ```

---

### AuthController (Member)

#### Register
- **Endpoint**: `POST /api/auth/register`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "name": "string (2-20 characters, Korean or English)",
    "username": "string (alias for name)",
    "userId": "string (4-20 characters, lowercase alphanumeric)",
    "password": "string (8-20 characters, 2+ of: letters, digits, special chars)",
    "passwordConfirm": "string (must match password)",
    "email": "string (valid email format)",
    "verificationCode": "string (optional)"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "회원가입이 완료되었습니다.",
    "data": {
      "id": "string",
      "userId": "string",
      "name": "string",
      "email": "string"
    }
  }
  ```

#### Send Verification Code
- **Endpoint**: `POST /api/auth/email/send-code`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "email": "string",
    "purpose": "string (register|reset)"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "인증번호가 발송되었습니다.",
    "data": {
      "email": "string",
      "expiresIn": 180
    }
  }
  ```

#### Verify Code
- **Endpoint**: `POST /api/auth/email/verify-code`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "email": "string",
    "verificationCode": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "인증이 완료되었습니다.",
    "data": {
      "verified": true
    }
  }
  ```

#### Check User ID Availability
- **Endpoint**: `GET /api/auth/check-userid?userId={userId}`
- **Authentication**: None
- **Query Parameters**: `userId` - User ID to check
- **Response**:
  ```json
  {
    "success": true,
    "message": "사용 가능한 아이디입니다.|이미 사용 중인 아이디입니다.",
    "data": {
      "available": "boolean"
    }
  }
  ```

#### Login
- **Endpoint**: `POST /api/auth/login`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "userId": "string (userId or email)",
    "password": "string",
    "userType": "string (normal|admin)"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "로그인 성공",
    "accessToken": "string",
    "token": "string (alias for accessToken)",
    "refreshToken": "string",
    "userId": "string",
    "username": "string",
    "email": "string",
    "role": "string"
  }
  ```
- **Note**: Account locks for 30 minutes after 5 failed attempts

#### Find User ID
- **Endpoint**: `POST /api/auth/find-userid`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "name": "string",
    "email": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "아이디를 찾았습니다.",
    "data": {
      "userId": "string",
      "maskedUserId": "string",
      "createdAt": "string (nullable)"
    }
  }
  ```

#### Verify User (Password Reset Step 1)
- **Endpoint**: `POST /api/auth/verify-user`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "userId": "string",
    "email": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "본인 확인이 완료되었습니다."
  }
  ```

#### Reset Password (Send Temp Password)
- **Endpoint**: `POST /api/auth/reset-password`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "name": "string",
    "userId": "string",
    "email": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "임시 비밀번호가 이메일로 발송되었습니다.",
    "data": {
      "email": "string (masked)"
    }
  }
  ```

#### Change Password
- **Endpoint**: `PUT /api/auth/change-password`
- **Authentication**: Required (Bearer token)
- **Request Body**:
  ```json
  {
    "currentPassword": "string",
    "newPassword": "string",
    "newPasswordConfirm": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "비밀번호가 변경되었습니다."
  }
  ```

#### Admin Reset Password
- **Endpoint**: `PUT /api/auth/admin/reset-password`
- **Authentication**: Required (Bearer token, Admin/Store Owner role)
- **Request Body**:
  ```json
  {
    "userId": "string",
    "newPassword": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "비밀번호가 초기화되었습니다."
  }
  ```

#### Refresh Token
- **Endpoint**: `POST /api/auth/refresh`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "refreshToken": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "토큰이 갱신되었습니다.",
    "data": {
      "accessToken": "string",
      "refreshToken": "string"
    }
  }
  ```

#### Logout
- **Endpoint**: `POST /api/auth/logout`
- **Authentication**: Optional (Bearer token)
- **Request Body**:
  ```json
  {
    "refreshToken": "string (optional)"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "로그아웃 되었습니다."
  }
  ```

---

### UserController

All endpoints require Bearer token authentication.

#### Get Profile
- **Endpoint**: `GET /api/users/profile`
- **Authentication**: Required (Bearer token)
- **Response**:
  ```json
  {
    "success": true,
    "message": "회원정보 조회 성공",
    "data": {
      "userId": "string",
      "name": "string",
      "email": "string",
      "userType": "string",
      "createdAt": "string (ISO datetime)"
    }
  }
  ```

#### Update Profile
- **Endpoint**: `PUT /api/users/profile`
- **Authentication**: Required (Bearer token)
- **Request Body**:
  ```json
  {
    "name": "string (2-20 characters)",
    "email": "string (valid email)"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "회원정보가 수정되었습니다.",
    "data": {
      "userId": "string",
      "name": "string",
      "email": "string",
      "userType": "string",
      "createdAt": "string (ISO datetime)"
    }
  }
  ```

#### Withdraw Member
- **Endpoint**: `DELETE /api/users/withdraw`
- **Authentication**: Required (Bearer token)
- **Response**:
  ```json
  {
    "success": true,
    "message": "회원탈퇴가 완료되었습니다.",
    "data": null
  }
  ```
- **Note**: Admin accounts cannot withdraw

---

## Order Service

Service responsible for cart and order management.

### AdminController (Order)

#### Get All Orders (Owner)
- **Endpoint**: `GET /api/owner/order`
- **Authentication**: None
- **Response**:
  ```json
  [
    {
      "orderId": "number",
      "customerId": "string",
      "customerName": "string",
      "orderDate": "datetime",
      "status": "string",
      "totalAmount": "number",
      "items": [
        {
          "orderItemId": "number",
          "menuCode": "number",
          "menuName": "string",
          "quantity": "number",
          "unitPrice": "number",
          "totalPrice": "number"
        }
      ]
    }
  ]
  ```

#### Reset Orders
- **Endpoint**: `POST /api/owner/order/reset`
- **Authentication**: None
- **Response**:
  ```json
  {
    "message": "주문 데이터가 초기화되었습니다."
  }
  ```

#### Get Order Detail (Owner)
- **Endpoint**: `GET /api/owner/order/{orderId}`
- **Authentication**: None
- **Path Variables**: `orderId` - Order ID
- **Response**: OrderDetailDto object

#### Update Order Status
- **Endpoint**: `PATCH /api/owner/order/{orderId}/status`
- **Authentication**: None
- **Path Variables**: `orderId` - Order ID
- **Response**: 200 OK (no body)
- **Description**: Advances order to next status (PENDING → CONFIRMED → PREPARING → READY → COMPLETED)

#### Get Statistics
- **Endpoint**: `GET /api/owner/total`
- **Authentication**: None
- **Response**:
  ```json
  {
    "totalOrders": "number",
    "totalRevenue": "decimal",
    "todayOrders": "number",
    "todayRevenue": "decimal"
  }
  ```

#### Cancel Order
- **Endpoint**: `PATCH /api/owner/order/{orderId}/cancel`
- **Authentication**: None
- **Path Variables**: `orderId` - Order ID
- **Response**: 200 OK (no body)

---

### OrderController

#### Get Cart
- **Endpoint**: `GET /api/order/cart`
- **Authentication**: None (uses hardcoded customerId: "1")
- **Response**:
  ```json
  [
    {
      "cartItemId": "number",
      "cartId": "number",
      "menuCode": "number",
      "menuName": "string",
      "quantity": "number",
      "unitPrice": "number",
      "totalAmount": "number",
      "options": [
        {
          "optionId": "number",
          "optionName": "string",
          "optionPrice": "number",
          "optionGroupName": "string"
        }
      ]
    }
  ]
  ```

#### Update Cart Item Quantity
- **Endpoint**: `PUT /api/order/cart/items/{cartItemId}`
- **Authentication**: None
- **Path Variables**: `cartItemId` - Cart item ID
- **Request Body**:
  ```json
  {
    "quantity": "number"
  }
  ```
- **Response**:
  ```json
  {
    "message": "수량이 변경되었습니다.",
    "cartItemId": "number",
    "quantity": "number"
  }
  ```

#### Delete Cart Item
- **Endpoint**: `DELETE /api/order/cart/items/{cartItemId}`
- **Authentication**: None
- **Path Variables**: `cartItemId` - Cart item ID
- **Response**:
  ```json
  {
    "message": "상품이 삭제되었습니다."
  }
  ```

#### Get Cart Count
- **Endpoint**: `GET /api/order/cart/count`
- **Authentication**: None (uses hardcoded customerId: "1")
- **Response**:
  ```json
  {
    "count": "number"
  }
  ```

#### Add Items to Cart
- **Endpoint**: `POST /api/order/add`
- **Authentication**: None (uses hardcoded customerId: "1")
- **Request Body**:
  ```json
  [
    {
      "customerId": "string",
      "menuCode": "number",
      "menuName": "string",
      "quantity": "number",
      "unitPrice": "number",
      "totalAmount": "number",
      "options": [
        {
          "optionId": "number",
          "optionName": "string",
          "optionPrice": "number",
          "optionGroupName": "string"
        }
      ]
    }
  ]
  ```
- **Response**: "장바구니(ID: {cartId})에 상품 {count}개가 저장되었습니다."

#### Place Order
- **Endpoint**: `POST /api/order/place`
- **Authentication**: None (uses hardcoded customerId: "1")
- **Request Body**:
  ```json
  {
    "request": "string (order notes)",
    "customerName": "string"
  }
  ```
- **Response** (HTTP 202 Accepted):
  ```json
  {
    "message": "주문이 접수되었습니다. 재고 확인 후 최종 확정됩니다.",
    "orderId": "number",
    "status": "PENDING"
  }
  ```

#### Get Order Detail
- **Endpoint**: `GET /api/order/detail/{orderId}`
- **Authentication**: None
- **Path Variables**: `orderId` - Order ID
- **Response**: OrderDetailDto object

#### Get Order History
- **Endpoint**: `GET /api/order/history`
- **Authentication**: None (uses hardcoded customerId: "1")
- **Query Parameters**:
  - `period` (optional): Period in months (1, 3, or all)
- **Response**:
  ```json
  [
    {
      "orderId": "number",
      "orderDate": "datetime",
      "status": "string",
      "totalAmount": "number",
      "items": [
        {
          "menuName": "string",
          "quantity": "number",
          "unitPrice": "number"
        }
      ]
    }
  ]
  ```

#### Test Add (Development)
- **Endpoint**: `GET /api/order/test-add`
- **Authentication**: None
- **Response**: Array of saved CartItem objects
- **Note**: Development endpoint for testing

#### Test Add 2 (Development)
- **Endpoint**: `GET /api/order/test-add2`
- **Authentication**: None
- **Response**: Array of saved CartItem objects
- **Note**: Development endpoint for testing

---

## Product Service

Service responsible for product/menu management and options.

### MenuController

#### Get Menu List
- **Endpoint**: `GET /api/menu/drinks`
- **Authentication**: None
- **Query Parameters**:
  - `category` (optional, multi-value): Filter by categories
  - `keyword` (optional): Search keyword
  - `page` (optional, default: 1): Page number
  - `limit` (optional, default: 20): Page size
- **Response**:
  ```json
  {
    "content": [
      {
        "menuCode": "number",
        "imageUrl": "string",
        "menuName": "string",
        "description": "string",
        "category": "string"
      }
    ],
    "pageable": {
      "pageNumber": "number",
      "pageSize": "number"
    },
    "totalPages": "number",
    "totalElements": "number"
  }
  ```

#### Search Menu
- **Endpoint**: `GET /api/menu/drinks/search`
- **Authentication**: None
- **Query Parameters**:
  - `keyword`: Search keyword
  - `page` (optional, default: 1): Page number
  - `limit` (optional, default: 20): Page size
- **Response**: Same as Get Menu List

#### Get Menu Detail
- **Endpoint**: `GET /api/menu/drinks/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Menu code
- **Response**:
  ```json
  {
    "menuCode": "number",
    "imageUrl": "string",
    "menuName": "string",
    "description": "string",
    "category": "string",
    "basePrice": "decimal",
    "baseVolume": "string",
    "allergies": [
      {
        "allergyId": "number",
        "allergyName": "string"
      }
    ],
    "nutrition": {
      "caffeine": "decimal",
      "calories": "decimal",
      "carbohydrates": "decimal",
      "protein": "decimal",
      "fat": "decimal",
      "sugar": "decimal",
      "sodium": "decimal"
    }
  }
  ```

---

### OptionController

#### Get Menu Options
- **Endpoint**: `GET /api/menu/options?menuCode={menuCode}`
- **Authentication**: None
- **Query Parameters**: `menuCode` - Menu code
- **Response**:
  ```json
  [
    {
      "optionGroupName": "string",
      "options": [
        {
          "optionId": "number",
          "optionName": "string",
          "optionPrice": "number"
        }
      ]
    }
  ]
  ```

---

### AdminAllergyController

#### Get All Allergies
- **Endpoint**: `GET /api/admin/allergies`
- **Authentication**: None
- **Response**:
  ```json
  [
    {
      "allergyId": "number",
      "allergyName": "string"
    }
  ]
  ```

---

### AdminCategoriController

#### Get All Categories
- **Endpoint**: `GET /api/admin/categori`
- **Authentication**: None
- **Response**:
  ```json
  [
    {
      "categoryName": "string"
    }
  ]
  ```

---

### AdminMaterialController

#### Get All Materials
- **Endpoint**: `GET /api/admin/materials`
- **Authentication**: None
- **Response**:
  ```json
  [
    {
      "ingredientId": "number",
      "ingredientName": "string",
      "baseUnit": "string"
    }
  ]
  ```

---

### AdminOptionController

#### Get All Options
- **Endpoint**: `GET /api/admin/options`
- **Authentication**: None
- **Response**:
  ```json
  [
    {
      "optionGroupName": "string",
      "options": [
        {
          "optionId": "number",
          "optionName": "string",
          "optionPrice": "number"
        }
      ]
    }
  ]
  ```

---

### AdminProductController

#### Get Product List (Admin)
- **Endpoint**: `GET /api/admin/products`
- **Authentication**: None
- **Query Parameters**:
  - `page` (optional, default: 1): Page number
  - `limit` (optional, default: 10): Page size
- **Response**:
  ```json
  {
    "content": [
      {
        "menuCode": "number",
        "menuName": "string",
        "category": "string",
        "basePrice": "decimal",
        "isAvailable": "boolean",
        "imageUrl": "string"
      }
    ],
    "totalPages": "number",
    "totalElements": "number"
  }
  ```

#### Create Product (Admin)
- **Endpoint**: `POST /api/admin/products`
- **Authentication**: None
- **Request Body**:
  ```json
  {
    "menuName": "string",
    "category": "string",
    "basePrice": "decimal",
    "baseVolume": "string",
    "description": "string",
    "isAvailable": "boolean",
    "imageUrl": "string (optional)",
    "allergyIds": ["number"],
    "nutrition": {
      "caffeine": "decimal",
      "calories": "decimal",
      "carbohydrates": "decimal",
      "protein": "decimal",
      "fat": "decimal",
      "sugar": "decimal",
      "sodium": "decimal"
    },
    "recipe": [
      {
        "ingredientId": "number",
        "requiredQty": "decimal"
      }
    ],
    "options": ["number"]
  }
  ```
- **Response**: Void (201 Created)

#### Update Product (Admin)
- **Endpoint**: `PUT /api/admin/products/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Menu code
- **Request Body**: Same as Create Product
- **Response**: Void (200 OK)

#### Get Product for Update (Admin)
- **Endpoint**: `GET /api/admin/products/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Menu code
- **Response**:
  ```json
  {
    "menuCode": "number",
    "menuName": "string",
    "category": "string",
    "basePrice": "decimal",
    "baseVolume": "string",
    "description": "string",
    "isAvailable": "boolean",
    "imageUrl": "string",
    "allergyIds": ["number"],
    "nutrition": {
      "caffeine": "decimal",
      "calories": "decimal",
      "carbohydrates": "decimal",
      "protein": "decimal",
      "fat": "decimal",
      "sugar": "decimal",
      "sodium": "decimal"
    },
    "recipe": [
      {
        "ingredientId": "number",
        "ingredientName": "string",
        "requiredQty": "decimal",
        "baseUnit": "string"
      }
    ],
    "options": ["number"]
  }
  ```

#### Delete Product (Admin)
- **Endpoint**: `DELETE /api/admin/products/{menuCode}`
- **Authentication**: None
- **Path Variables**: `menuCode` - Menu code
- **Response**:
  ```json
  {
    "message": "제품이 삭제되었습니다."
  }
  ```

#### Search Products (Admin)
- **Endpoint**: `GET /api/admin/products/search`
- **Authentication**: None
- **Query Parameters**:
  - `keyword`: Search keyword
  - `page` (optional, default: 1): Page number
  - `limit` (optional, default: 20): Page size
- **Response**: Same as Get Product List

---

## Common Patterns and Notes

### Authentication
- **JWT Bearer Token**: Most authenticated endpoints expect `Authorization: Bearer {token}` header
- **Request Attributes**: Some services use JWT interceptors that extract userId and role into request attributes
- **Admin Role Check**: Admin endpoints typically check for "admin" or "store_owner" role

### Pagination
- **Page Indexing**: Some services use 0-based (admin-service, member-service), others use 1-based (board-service, product-service)
- **Default Sizes**: Typically 10-20 items per page
- **Maximum Sizes**: Usually capped at 50 items per page

### Error Responses
Most services return error responses in similar formats:
```json
{
  "success": false,
  "message": "Error message in Korean",
  "errorCode": "ERROR_CODE"
}
```

Or simply a string message for simple errors.

### URL Encoding
- Korean text in query parameters should be URL encoded
- Services typically decode using UTF-8 charset

### File Uploads
- Product images: Max 5MB, jpg/png/webp formats
- Upload directory configured via `${file.upload-dir}` property
- Images served from `/uploads/products/` path

### CORS and Proxy
- Frontend service acts as API gateway/proxy
- All API requests from frontend go through ApiProxyController
- Headers are preserved and forwarded to backend services

---

## Service URLs

Default service URLs (can be overridden via environment variables):

- **Frontend Service**: Port 8005
- **Admin Service**: Port 8007
- **Board Service**: Port 8006
- **Inventory Service**: Port 8008
- **Member Service**: Port 8004
- **Order Service**: Port 8002
- **Product Service**: Port 8001

---

## Data Types Reference

- `string`: Text data (UTF-8)
- `number`: Integer
- `decimal`: Decimal/floating point (BigDecimal)
- `boolean`: true/false
- `datetime`: ISO 8601 datetime format or LocalDateTime
- `array`: JSON array
- `object`: JSON object
- `nullable`: Field may be null

---

*Document generated: 2026-04-14*
*Total endpoints documented: 100+*