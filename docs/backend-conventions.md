# Backend Conventions - Actual Code Patterns

This document describes the **actual coding patterns and conventions** used in this project, based on real code analysis. It shows what the codebase actually does, including inconsistencies and variations across services.

**Analysis Date:** 2026-04-14
**Services Analyzed:** member-service, product-service, order-service, inventory-service, board-service, admin-service

---

## 1. Package Structure

### 1.1 Base Package Naming - **INCONSISTENT**

Different services use different base package names:

```
member-service:    com.example.member.*
product-service:   com.example.product.*
order-service:     com.example.cust.*          ❌ (inconsistent naming - "cust" instead of "order")
inventory-service: com.example.inventory.*
board-service:     com.example.boardservice.*  ❌ (inconsistent - concatenated name)
admin-service:     com.du.adminservice.*       ❌ (different organization - "du" instead of "example")
```

**Observation:** Most services use `com.example.{service-name}`, but there are notable exceptions.

### 1.2 Standard Package Structure

Most services follow this structure:

```
src/main/java/com/{org}/{service}/
├── config/              # Configuration classes (SecurityConfig, CorsConfig, WebConfig, etc.)
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs (member-service only)
│   ├── response/        # Response DTOs (member-service only)
│   ├── admin/           # Admin-specific DTOs (product-service)
│   └── user/            # User-facing DTOs (product-service)
├── model/               # JPA Entities (also called "entity" in some contexts)
├── repository/          # JPA Repositories
├── service/             # Business logic services
│   └── admin/           # Admin-specific services (product-service)
├── util/                # Utility classes (JwtUtil, etc.)
├── enums/               # Enums (member-service only)
└── messaging/           # Message listeners (inventory-service only)
```

**Examples:**

**member-service** - Most organized structure:
```
C:\Users\wtme4\OneDrive\바탕 화면\aitool\teamprojectv1\member-service\src\main\java\com\example\member\
├── config/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── enums/
├── model/
├── repository/
├── service/
└── util/
```

**product-service** - Organized by user/admin:
```
C:\Users\wtme4\OneDrive\바탕 화면\aitool\teamprojectv1\product-service\src\main\java\com\example\product\
├── controller/
│   └── admin/
├── dto/
│   ├── admin/
│   └── user/
├── model/
├── repository/
└── service/
    └── admin/
```

**board-service** - Simple structure (no dto package):
```
C:\Users\wtme4\OneDrive\바탕 화면\aitool\teamprojectv1\board-service\src\main\java\com\example\boardservice\
├── config/
├── controller/
├── model/
├── repository/
├── service/
└── util/
```

### 1.3 Key Observations

- **DTO organization varies:** member-service uses `request/response` subdirectories, product-service uses `admin/user`, board-service has no separate DTOs
- **Not all services have util packages:** Only member-service and board-service have explicit util packages
- **Configuration packages are common:** All services have a `config/` package for Spring configurations

---

## 2. Naming Conventions

### 2.1 Class Naming Patterns

#### Controllers

Pattern: `{Entity}Controller` or `{Function}{Entity}Controller`

**Examples:**
```java
// Standard pattern
AuthController.java              // member-service/controller/AuthController.java
UserController.java              // member-service/controller/UserController.java
MenuController.java              // product-service/controller/MenuController.java
OrderController.java             // order-service/controller/OrderController.java
BoardController.java             // board-service/controller/BoardController.java

// Admin-specific pattern
AdminController.java             // member-service/controller/AdminController.java
AdminUserController.java         // admin-service/controller/AdminUserController.java
AdminProductController.java      // admin-service/controller/AdminProductController.java
AdminProductController.java      // product-service/controller/admin/AdminProductController.java
```

#### Services

Pattern: `{Entity}Service` or `{Entity}ServiceImpl`

**Examples:**
```java
// Interface pattern (rare)
InventoryService.java            // inventory-service/service/InventoryService.java
InventoryServiceImpl.java        // inventory-service/service/InventoryServiceImpl.java

// Concrete class pattern (most common) ✅
MemberService.java               // member-service/service/MemberService.java
BoardService.java                // board-service/service/BoardService.java
OrderService.java                // order-service/service/OrderService.java
MenuService.java                 // product-service/service/MenuService.java
EmailService.java                // member-service/service/EmailService.java

// Specialized services
CustomOAuth2UserService.java     // member-service/service/CustomOAuth2UserService.java
CartDetailService.java           // order-service/service/CartDetailService.java
MakeCart.java                    // ❌ order-service/service/MakeCart.java (inconsistent naming)
```

**Observation:** Most services are concrete classes without interfaces. Only inventory-service uses the interface/implementation pattern.

#### Repositories

Pattern: `{Entity}Repository` (extends `JpaRepository`)

**Examples:**
```java
MemberRepository extends JpaRepository<Member, String>        // member-service
MenuRepository extends JpaRepository<Menu, Long>              // product-service
OrdersRepository extends JpaRepository<Orders, Integer>       // order-service
BoardRepository extends JpaRepository<Board, Long>            // board-service
```

All repositories follow the standard Spring Data JPA naming convention.

#### Entities (Models)

Pattern: `{EntityName}` (singular noun)

**Examples:**
```java
Member.java                      // member-service/model/Member.java
Menu.java                        // product-service/model/Menu.java
Orders.java                      // ⚠️ order-service/model/Orders.java (plural to avoid SQL keyword)
Board.java                       // board-service/model/Board.java
CartHeader.java                  // order-service/model/CartHeader.java
CartItem.java                    // order-service/model/CartItem.java
MaterialMaster.java              // inventory-service/model/MaterialMaster.java
```

**Observation:** Entity names are singular, except `Orders` which uses plural to avoid the SQL `ORDER` keyword.

#### DTOs

Pattern varies significantly:

**member-service pattern:**
```java
RegisterRequest.java             // dto/request/RegisterRequest.java
UpdateProfileRequest.java        // dto/request/UpdateProfileRequest.java
ApiResponse.java                 // dto/response/ApiResponse.java
ProfileResponse.java             // dto/response/ProfileResponse.java
```

**product-service pattern:**
```java
MenuListDto.java                 // dto/user/MenuListDto.java
MenuDetailDto.java               // dto/user/MenuDetailDto.java
AdminProductListDto.java         // dto/admin/AdminProductListDto.java
AdminProductCreateUpdateDto.java // dto/admin/AdminProductCreateUpdateDto.java
```

**order-service pattern:**
```java
OrderDetailDto.java              // dto/OrderDetailDto.java
OrderHistoryDto.java             // dto/OrderHistoryDto.java
ProductItemDto.java              // dto/ProductItemDto.java
```

**inventory-service pattern (nested static classes):**
```java
// InventoryDto.java contains:
public class InventoryDto {
    public static class Response { ... }
    public static class CreateRequest { ... }
    public static class AddRequest { ... }
    public static class UpdateRequest { ... }
    public static class UpdateResponse { ... }
}
```

**board-service pattern (inner classes in Controller):**
```java
// BoardController.java contains:
public static class CreateBoardRequest { ... }
public static class UpdateBoardRequest { ... }
```

**Key Observations:**
- **No consistent DTO naming standard**
- member-service: `{Action}Request/Response`
- product-service: `{Entity}{Purpose}Dto`
- inventory-service: Nested static classes in `{Entity}Dto`
- board-service: Inner classes in Controllers (not recommended)

#### Exception/Error Classes

**Custom exceptions are NOT widely used** in this project. Instead, the code uses:
- `RuntimeException` with custom messages (most common)
- `IllegalArgumentException` for validation errors
- ErrorCode enum (only in member-service)

**Examples:**
```java
// member-service/enums/ErrorCode.java
public enum ErrorCode {
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다."),
    DUPLICATE_USER_ID("이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다."),
    // ... more error codes
}

// Common pattern in services:
throw new RuntimeException("USER_NOT_FOUND");
throw new RuntimeException("이미 존재하는 아이디입니다.");
throw new IllegalArgumentException("메뉴를 찾을 수 없습니다.");
```

**Observation:** No custom exception classes are defined. Error handling relies on standard Java exceptions and string messages.

### 2.2 Method Naming Patterns

#### Controller Methods

**Pattern:** HTTP verb + entity/action

**Examples:**
```java
// member-service/controller/AuthController.java
public ResponseEntity<?> register(@RequestBody RegisterRequest request)
public ResponseEntity<?> login(@RequestBody Map<String, String> request)
public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request)
public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request)
public ResponseEntity<?> checkUserId(@RequestParam String userId)
public ResponseEntity<?> findUserId(@RequestBody Map<String, String> request)
public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request)
public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader, ...)
public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request)
public ResponseEntity<?> logout(...)

// product-service/controller/MenuController.java
public Page<MenuListDto> getMenuList(...)
public Page<MenuListDto> searchMenu(...)
public MenuDetailDto getMenuDetail(@PathVariable Long menuCode)

// order-service/controller/OrderController.java
public ResponseEntity<List<CartItem>> getCart()
public ResponseEntity<?> updateCartItemQuantity(...)
public ResponseEntity<?> deleteCartItem(@PathVariable Long cartItemId)
public ResponseEntity<?> getCartCount()
public ResponseEntity<String> addItemsToCart(@RequestBody List<ProductItemDto> productItems)
public ResponseEntity<Map<String, Object>> placeOrder(...)
public ResponseEntity<?> getOrderDetail(@PathVariable("orderId") Integer orderId)
public ResponseEntity<List<OrderHistoryDto>> getOrderHistory(...)
```

**Pattern breakdown:**
- GET: `get{Entity}`, `get{Entity}List`, `search{Entity}`
- POST: `{action}{Entity}` (create, add, place, send, verify)
- PUT: `update{Entity}`, `change{Entity}`
- DELETE: `delete{Entity}`

#### Service Methods

**Pattern:** action + entity (no "get" prefix often omitted)

**Examples:**
```java
// member-service/service/MemberService.java
public Member register(String name, String userId, String password, ...)
public String login(String userId, String password)
public Member findByNameAndEmail(String name, String email)
public String maskUserId(String userId)
public String generateTempPassword()
public void updatePassword(Member member, String newPassword)
public void changePassword(String userId, String currentPassword, String newPassword)
public Member updateProfile(String userId, String name, String email)
public boolean isUserIdAvailable(String userId)
public Member getMemberByUserId(String userId)
public List<Member> getAllMembers()
public List<Member> searchByKeyword(String keyword)
public void deleteMember(String userId)

// board-service/service/BoardService.java
public List<Board> getAllBoards()
public Board getBoardById(Long id)
public Board createBoard(Board board)
public Board updateBoard(Long id, Board updatedBoard, String userId)
public void deleteBoard(Long id, String userId)
public List<Board> getBoardsByUserId(String userId)

// product-service/service/MenuService.java
public Page<MenuListDto> getMenuList(List<String> categories, String keyword, ...)
public Page<MenuListDto> searchMenus(String keyword, int page, int limit)
public MenuDetailDto getMenuDetail(Long menuCode)
```

**Pattern breakdown:**
- Query: `get{Entity}`, `find{Entity}`, `search{Entity}`, `getAll{Entity}s`
- Create: `create{Entity}`, `register{Entity}`, `save{Entity}`
- Update: `update{Entity}`, `change{Entity}`
- Delete: `delete{Entity}`
- Validation: `is{Property}Available`, `validate{Entity}`
- Conversion: `mask{Property}`, `generate{Property}`, `to{Type}Dto`

### 2.3 Variable Naming Patterns

#### Field Variables

**Pattern:** camelCase

**Examples:**
```java
// Controllers
private final MemberService memberService;
private final MemberRepository memberRepository;
private final EmailService emailService;
private final JwtUtil jwtUtil;

// Services
private final MenuRepository menuRepository;
private final NutritionRepository nutritionRepository;
private final AllergyRepository allergyRepository;

// Constants
private static final Logger log = LoggerFactory.getLogger(MemberService.class);
private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
```

#### Method Parameters

**Pattern:** camelCase, descriptive names

**Examples:**
```java
public Member register(String name, String userId, String password, String email,
                       LocalDateTime createdAt, String userType)

public Page<MenuListDto> getMenuList(List<String> categories, String keyword,
                                      int page, int limit)

public ResponseEntity<?> updateCartItemQuantity(@PathVariable Long cartItemId,
                                                 @RequestBody Map<String, Integer> request)
```

#### Local Variables

**Pattern:** camelCase, short but descriptive

**Examples:**
```java
// member-service/controller/AuthController.java
String userId = request.get("userId");
String password = request.get("password");
String userType = request.get("userType");
Member member = memberRepository.findByUserId(trimmedUserId).orElse(null);
String accessToken = jwtUtil.generateAccessToken(member.getUserId(), member.getUserType());

// order-service/service/OrderService.java
CartHeader cartHeader = cartDetailService.getCartHeaderByCustomerId(customerId);
List<CartItem> cartItems = cartHeader.getCartItems();
int totalOrderAmount = cartItems.stream().mapToInt(CartItem::getTotalItemPrice).sum();
```

---

## 3. Exception Handling

### 3.1 GlobalExceptionHandler - **NOT USED**

**Finding:** None of the services implement `@RestControllerAdvice` or `@ControllerAdvice` for global exception handling.

**Evidence:**
```bash
# Search results:
@RestControllerAdvice|@ControllerAdvice
> No files found
```

### 3.2 Try-Catch in Controllers - **EXTENSIVELY USED**

Controllers handle exceptions locally with try-catch blocks.

**Statistics:**
- 69 try-catch blocks found across 15 controller files
- Average: 4-5 try-catch blocks per controller

**Pattern 1: Map-based error responses (most common):**

```java
// member-service/controller/AuthController.java:38-167
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    try {
        // Validation logic
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "이름을 입력해주세요.");
            error.put("errorCode", "BAD_REQUEST");
            return ResponseEntity.badRequest().body(error);
        }

        // ... more validation ...

        // Business logic
        Member saved = memberRepository.save(member);

        // Success response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다.");
        response.put("data", responseData);
        return ResponseEntity.status(201).body(response);

    } catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        error.put("errorCode", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(500).body(error);
    }
}
```

**Pattern 2: Simple error message (less common):**

```java
// board-service/controller/BoardController.java:42-77
@PostMapping
public ResponseEntity<?> createBoard(@RequestBody CreateBoardRequest request,
                                     HttpServletRequest httpRequest) {
    try {
        String userId = (String) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.badRequest().body("로그인이 필요합니다.");
        }

        Board savedBoard = boardService.createBoard(board);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "게시글이 작성되었습니다.");
        response.put("boardId", savedBoard.getInquiryId());

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("게시글 작성에 실패했습니다: " + e.getMessage());
    }
}
```

**Pattern 3: Specific exception handling:**

```java
// order-service/controller/OrderController.java:48-66
@PutMapping("/cart/items/{cartItemId}")
public ResponseEntity<?> updateCartItemQuantity(@PathVariable Long cartItemId,
                                                @RequestBody Map<String, Integer> request) {
    try {
        int quantity = request.get("quantity");
        cartDetailService.updateQuantity(cartItemId, quantity);

        return ResponseEntity.ok(Map.of(
                "message", "수량이 변경되었습니다.",
                "cartItemId", cartItemId,
                "quantity", quantity
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 실패");
    }
}
```

**Key Observations:**
- Controllers contain **extensive validation logic** (checking null, empty, format)
- Try-catch blocks are **nested inside every endpoint method**
- Error responses are **manually constructed** using Map or simple strings
- **No standardized error response format** across services
- **e.printStackTrace()** is used frequently (should use logger)

### 3.3 Try-Catch in Services - **RARELY USED**

Services mostly throw exceptions and let controllers handle them.

**Examples:**

```java
// member-service/service/MemberService.java
public Member register(String name, String userId, String password, String email,
                       LocalDateTime createdAt, String userType) {
    if (memberRepository.existsByUserId(userId)) {
        throw new RuntimeException("이미 존재하는 아이디입니다.");
    }
    if (memberRepository.existsByEmail(email)) {
        throw new RuntimeException("이미 존재하는 이메일입니다.");
    }
    // ... business logic ...
    return memberRepository.save(member);
}

public Member getBoardById(Long id) {
    return boardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
}

// product-service/service/MenuService.java
public Page<MenuListDto> searchMenus(String keyword, int page, int limit) {
    if (keyword == null || keyword.isBlank()) {
        throw new IllegalArgumentException("검색어를 입력해 주세요.");
    }
    // ... business logic ...
}
```

**Pattern:**
- Services throw `RuntimeException` or `IllegalArgumentException`
- Controllers catch these and convert to HTTP responses
- No try-catch in service methods (clean separation)

### 3.4 Exception Handling Summary

| Approach | Usage | Files | Percentage |
|----------|-------|-------|-----------|
| Try-catch in Controllers | ✅ Used extensively | 15 files, 69 occurrences | ~100% of controllers |
| GlobalExceptionHandler | ❌ Not used | 0 files | 0% |
| Service throws exceptions | ✅ Standard pattern | All services | ~100% |
| Custom exception classes | ❌ Not used | 0 files | 0% |

**Conclusion:** This project relies on **controller-level try-catch blocks** instead of centralized exception handling. This leads to code duplication but provides fine-grained control over error responses.

---

## 4. DTO Conversion Patterns

### 4.1 Builder Pattern - **MOST COMMON** ✅

The builder pattern (via Lombok `@Builder`) is the dominant DTO conversion method.

**Examples:**

```java
// product-service/service/MenuService.java:55-60
return menus.map(menu -> MenuListDto.builder()
        .menuCode(menu.getMenuCode())
        .menuName(menu.getMenuName())
        .description(menu.getDescription())
        .category(menu.getCategory())
        .build());

// product-service/service/MenuService.java:117-127
return MenuDetailDto.builder()
        .menuCode(menu.getMenuCode())
        .imageUrl(buildImageUrl(menu.getMenuName()))
        .menuName(menu.getMenuName())
        .description(menu.getDescription())
        .category(menu.getCategory())
        .basePrice(menu.getBasePrice())
        .baseVolume(menu.getBaseVolume())
        .allergies(allergies)
        .nutrition(nutritionDto)
        .build();

// order-service/service/OrderService.java:48-55
Orders order = Orders.builder()
        .orderDate(LocalDateTime.now())
        .customerId(customerId)
        .customerName(customerName)
        .totalAmount(totalOrderAmount)
        .status(OrderStatus.PENDING)
        .request(requestMessage)
        .build();

// inventory-service/dto/InventoryDto.java:13
@Builder
public static class Response {
    private Integer ingredientId;
    private String ingredientName;
    private String baseUnit;
    private BigDecimal stockQty;
}
```

**Usage:**
- Entity → DTO conversion in services
- DTO → Entity conversion (less common)
- Nested builders for complex objects

### 4.2 Manual Field Copying with Setters - **COMMON IN CONTROLLERS**

Controllers often use manual setters when creating entities from requests.

**Examples:**

```java
// member-service/controller/AuthController.java:134-140
Member member = new Member();
member.setName(request.getName().trim());
member.setUserId(request.getUserId().trim());
member.setPassword(passwordEncoder.encode(request.getPassword()));
member.setEmail(request.getEmail().trim());
member.setUserType("member");

// board-service/controller/BoardController.java:61-64
Board board = new Board();
board.setTitle(request.getTitle().trim());
board.setContent(request.getContent().trim());
board.setUserId(userId);
```

**Pattern:** Used when creating entities from user input (POST/PUT requests).

### 4.3 Static Factory Methods - **RARELY USED**

Found only in ApiResponse DTO:

```java
// member-service/dto/response/ApiResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }
}
```

**Note:** This class is defined but **NOT actually used** in the AuthController. Instead, controllers use `Map<String, Object>` for responses.

### 4.4 Stream Mapping - **COMMON FOR COLLECTIONS**

Used extensively when converting lists/collections.

**Examples:**

```java
// order-service/service/OrderService.java:58-84
List<OrderItem> orderItems = cartItems.stream()
        .map(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .menuCode(cartItem.getMenuCode())
                    .menuName(cartItem.getMenuName())
                    .quantity(cartItem.getQuantity())
                    .priceAtOrder(cartItem.getUnitPrice())
                    .totalItemPrice(cartItem.getTotalItemPrice())
                    .order(order)
                    .build();

            List<OrderOption> orderOptions = cartItem.getCartOptions().stream()
                    .map(cartOption -> OrderOption.builder()
                            .optionId(cartOption.getOptionId())
                            .optionName(cartOption.getOptionName())
                            .optionPriceAtOrder(cartOption.getOptionPrice())
                            .orderItem(orderItem)
                            .build())
                    .collect(Collectors.toList());

            orderItem.getOrderOptions().addAll(orderOptions);
            return orderItem;
        })
        .collect(Collectors.toList());

// order-service/service/OrderService.java:198-206
return orders.stream()
        .map(order -> OrderHistoryDto.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().getDescription())
                .itemCount(order.getOrderItems().size())
                .build())
        .collect(Collectors.toList());
```

### 4.5 ModelMapper / MapStruct - **NOT USED**

**Finding:** No dependency injection frameworks like ModelMapper or MapStruct are used.

**Evidence:** No `@Mapper` annotations or `modelMapper.map()` calls found in the codebase.

### 4.6 DTO Conversion Summary

| Method | Usage Frequency | Use Case | Example Location |
|--------|----------------|----------|------------------|
| Builder Pattern | ✅ Very High | Entity → DTO | MenuService.java:55, OrderService.java:48 |
| Manual Setters | ✅ High | Request → Entity | AuthController.java:134, BoardController.java:61 |
| Stream + Builder | ✅ High | List conversions | OrderService.java:58, OrderService.java:198 |
| Static Factory | ⚠️ Defined but unused | Response wrappers | ApiResponse.java |
| ModelMapper/MapStruct | ❌ Not used | N/A | N/A |

**Conclusion:** This project uses **Lombok @Builder** as the primary DTO conversion mechanism, combined with manual setters for request handling and stream mapping for collections.

---

## 5. Dependency Injection

### 5.1 Constructor Injection with @RequiredArgsConstructor - **MOST COMMON** ✅

**Statistics:**
- Found in 11 service files
- Used in most modern controllers and services

**Examples:**

```java
// order-service/controller/OrderController.java:22-30
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final MakeCart makeCartService;
    private final CartDetailService cartDetailService;
    private final OrderService orderService;

    // No constructor needed - Lombok generates it
}

// product-service/service/MenuService.java:24-30
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final NutritionRepository nutritionRepository;
    private final AllergyRepository allergyRepository;

    // No constructor needed - Lombok generates it
}

// order-service/service/OrderService.java:17-25
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final CartDetailService cartDetailService;
    private final CartHeaderRepository cartHeaderRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    // No constructor needed
}
```

### 5.2 Manual Constructor Injection - **COMMON IN OLDER CODE**

Some services use explicit constructors instead of @RequiredArgsConstructor.

**Examples:**

```java
// member-service/controller/AuthController.java:19-34
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(MemberService memberService, MemberRepository memberRepository,
                          EmailService emailService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();  // ❌ NOT injected!
    }
}

// member-service/service/MemberService.java:18-31
@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
}

// board-service/controller/BoardController.java:15-21
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }
}
```

### 5.3 @Autowired Field Injection - **NOT USED** ✅

**Finding:** No `@Autowired` field injection found in the codebase.

**Evidence:** All dependency injection uses constructor-based injection (manually or via @RequiredArgsConstructor).

### 5.4 Dependency Injection Summary

| Method | Usage | Count | Percentage |
|--------|-------|-------|-----------|
| @RequiredArgsConstructor | ✅ Preferred (modern code) | 11+ files | ~60% |
| Manual Constructor Injection | ✅ Used (older code) | 5+ files | ~40% |
| @Autowired Field Injection | ❌ Not used | 0 files | 0% |

**Trend:** Newer services (order, product, inventory) use @RequiredArgsConstructor, while older services (member, board, admin) use manual constructors.

**Anti-pattern found:**
```java
// member-service/controller/AuthController.java:33
this.passwordEncoder = new BCryptPasswordEncoder();  // ❌ Should be injected
```

---

## 6. Transaction Management

### 6.1 @Transactional Usage in Services

**Statistics:**
- 36 @Transactional occurrences across 7 service files
- Average: 5-6 transactional methods per service

**Examples:**

```java
// member-service/service/MemberService.java:34-55
@Transactional
public Member register(String name, String userId, String password, String email,
                       LocalDateTime createdAt, String userType) {
    if (memberRepository.existsByUserId(userId)) {
        throw new RuntimeException("이미 존재하는 아이디입니다.");
    }
    if (memberRepository.existsByEmail(email)) {
        throw new RuntimeException("이미 존재하는 이메일입니다.");
    }
    // ... business logic ...
    return memberRepository.save(member);
}

@Transactional
public void changePassword(String userId, String currentPassword, String newPassword) {
    Member member = memberRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    // ... validation ...
    member.setPassword(passwordEncoder.encode(newPassword));
    memberRepository.save(member);
}

// order-service/service/OrderService.java:35-118
@Transactional
public Orders placeOrder(String customerId, String customerName, String requestMessage) {
    // 1. 장바구니 조회
    CartHeader cartHeader = cartDetailService.getCartHeaderByCustomerId(customerId);
    // 2. 주문 생성
    Orders order = Orders.builder()...build();
    // 3. 주문 아이템 변환
    List<OrderItem> orderItems = cartItems.stream()...collect();
    // 4. 저장
    Orders savedOrder = ordersRepository.save(order);
    // 5. 장바구니 삭제
    cartHeaderRepository.delete(cartHeader);
    return savedOrder;
}

@Transactional(readOnly = true)
public OrderDetailDto getOrderDetail(Integer orderId) {
    Orders order = ordersRepository.findDetailByIdWithItemsAndOptions(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문 ID를 찾을 수 없습니다: " + orderId));
    return toDetailDto(order);
}
```

### 6.2 Transaction Boundaries

**Pattern 1: Write operations use @Transactional**
```java
@Transactional  // Default propagation = REQUIRED
public Member register(...) { }

@Transactional
public void updatePassword(...) { }

@Transactional
public Member updateProfile(...) { }

@Transactional
public void deleteMember(String userId) { }
```

**Pattern 2: Read operations use @Transactional(readOnly = true)**
```java
@Transactional(readOnly = true)
public OrderDetailDto getOrderDetail(Integer orderId) { }

@Transactional(readOnly = true)
public List<OrderHistoryDto> getOrderHistoryList(String customerId) { }

@Transactional(readOnly = true)
public List<OrderHistoryDto> getOrderHistoryListByPeriod(String customerId, Integer months) { }
```

**Pattern 3: Simple read operations - NO @Transactional**
```java
// Most repository queries don't use @Transactional
public Member getMemberByUserId(String userId) {
    return memberRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
}

public List<Board> getAllBoards() {
    return boardRepository.findAllByOrderByCreatedAtDesc();
}
```

### 6.3 Propagation Settings

**Finding:** No custom propagation settings used. All transactions use default `REQUIRED`.

**Evidence:** All `@Transactional` annotations have no propagation parameter.

### 6.4 Transaction Management Summary

| Usage Pattern | Frequency | Example |
|---------------|-----------|---------|
| @Transactional on write operations | ✅ High | register, update, delete methods |
| @Transactional(readOnly=true) on reads | ⚠️ Moderate | Complex queries, DTO conversions |
| No @Transactional on simple reads | ✅ High | Simple findById, getAll operations |
| Custom propagation | ❌ Not used | N/A |

**Best Practice Observations:**
- ✅ Write operations consistently use @Transactional
- ✅ Complex multi-entity operations use transactions
- ✅ Read-only flag used for optimization
- ⚠️ Some simple reads could benefit from readOnly transactions
- ✅ No transaction management in controllers (correct separation)

---

## 7. API Response Format

### 7.1 ResponseEntity<T> - **UNIVERSAL** ✅

All controllers use `ResponseEntity<?>` or `ResponseEntity<T>` for HTTP responses.

**Examples:**

```java
// Type-specific ResponseEntity
public ResponseEntity<List<Board>> getAllBoards()
public ResponseEntity<Board> getBoardById(@PathVariable Long id)
public ResponseEntity<List<CartItem>> getCart()
public ResponseEntity<List<OrderHistoryDto>> getOrderHistory(...)
public Page<MenuListDto> getMenuList(...)  // ❌ Exception - returns plain object

// Generic ResponseEntity<?>
public ResponseEntity<?> register(@RequestBody RegisterRequest request)
public ResponseEntity<?> login(@RequestBody Map<String, String> request)
public ResponseEntity<?> updateCartItemQuantity(...)
public ResponseEntity<?> deleteCartItem(@PathVariable Long cartItemId)
```

### 7.2 Response Body Structures - **HIGHLY INCONSISTENT** ❌

The project has **no standardized response format**. Different services use different structures:

#### Pattern 1: Map with success/message/data (member-service)

```java
// member-service/controller/AuthController.java:152-157
Map<String, Object> response = new HashMap<>();
response.put("success", true);
response.put("message", "회원가입이 완료되었습니다.");
response.put("data", responseData);
return ResponseEntity.status(201).body(response);

// Error response
Map<String, Object> error = new HashMap<>();
error.put("success", false);
error.put("message", "이름을 입력해주세요.");
error.put("errorCode", "BAD_REQUEST");
return ResponseEntity.badRequest().body(error);
```

**Structure:**
```json
{
  "success": true/false,
  "message": "메시지",
  "data": { ... },
  "errorCode": "ERROR_CODE"  // Only on errors
}
```

#### Pattern 2: Map with message/data (order-service)

```java
// order-service/controller/OrderController.java:58-62
return ResponseEntity.ok(Map.of(
        "message", "수량이 변경되었습니다.",
        "cartItemId", cartItemId,
        "quantity", quantity
));

// Error response
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 실패");
```

**Structure:**
```json
{
  "message": "메시지",
  "cartItemId": 123,
  "quantity": 5
}
```

#### Pattern 3: Direct DTO/Entity (product-service, board-service)

```java
// product-service/controller/MenuController.java:42-44
public MenuDetailDto getMenuDetail(@PathVariable Long menuCode) {
    return menuService.getMenuDetail(menuCode);
}

// board-service/controller/BoardController.java:25-27
public ResponseEntity<List<Board>> getAllBoards() {
    return ResponseEntity.ok(boardService.getAllBoards());
}
```

**Structure:** Plain DTO/Entity without wrapper
```json
{
  "menuCode": 1,
  "menuName": "아메리카노",
  "description": "...",
  "basePrice": 2500
}
```

#### Pattern 4: String messages

```java
// order-service/controller/OrderController.java:108-109
return ResponseEntity.ok(String.format("장바구니(ID: %d)에 상품 %d개가 저장되었습니다.",
        cartHeader.getCartId(), savedItems.size()));

// board-service/controller/BoardController.java:75
return ResponseEntity.badRequest().body("게시글 작성에 실패했습니다: " + e.getMessage());
```

### 7.3 ApiResponse Wrapper - **DEFINED BUT UNUSED** ⚠️

```java
// member-service/dto/response/ApiResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
}
```

**Status:** This class exists but is **NOT used anywhere** in the actual controllers. All controllers use `Map<String, Object>` instead.

### 7.4 HTTP Status Code Usage

**Common patterns:**

```java
// Success responses
ResponseEntity.ok(...)                    // 200 OK
ResponseEntity.status(201).body(...)      // 201 Created
ResponseEntity.status(HttpStatus.ACCEPTED)... // 202 Accepted (async operations)

// Error responses
ResponseEntity.badRequest().body(...)     // 400 Bad Request
ResponseEntity.status(HttpStatus.UNAUTHORIZED)... // 401 Unauthorized
ResponseEntity.status(HttpStatus.FORBIDDEN)... // 403 Forbidden
ResponseEntity.status(HttpStatus.NOT_FOUND)... // 404 Not Found
ResponseEntity.status(409).body(...)      // 409 Conflict (duplicate)
ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)... // 429 Too Many Requests
ResponseEntity.status(500).body(...)      // 500 Internal Server Error
ResponseEntity.internalServerError().body(...) // 500 (alternative)
```

### 7.5 API Response Summary

| Response Type | Usage | Services |
|---------------|-------|----------|
| ResponseEntity<?> | ✅ Universal | All services |
| Map<String, Object> with success/message/data | ✅ Common | member-service, admin-service |
| Map with custom fields | ✅ Common | order-service |
| Direct DTO/Entity | ✅ Common | product-service, board-service |
| Plain String | ⚠️ Occasional | order-service, board-service |
| ApiResponse<T> wrapper | ❌ Unused | None (defined in member-service) |

**Conclusion:** The project has **NO consistent API response format**. Each service implements its own pattern, making frontend integration more complex.

---

## 8. Logging

### 8.1 Logging Frameworks Used

#### @Slf4j (Lombok) - **MODERATE USAGE**

**Statistics:**
- 22 occurrences across 22 files
- Used in newer services

**Examples:**

```java
// order-service/controller/OrderController.java:22-24
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    log.info("[주문] 받은 요청 데이터 - requestMessage: {}, customerName: {}", requestMessage, customerName);
    log.info("[주문] 전체 requestBody: {}", requestBody);
    log.info("[주문] 주문 접수 완료 - orderId: {}, customerName: {}, status: PENDING",
            savedOrder.getOrderId(), savedOrder.getCustomerName());
    log.error("[주문] 주문 실패: ", e);
}

// order-service/service/OrderService.java:17-18
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    log.error("Failed to publish order stock message: {}", e.getMessage(), e);
}
```

#### private static final Logger - **LOW USAGE**

**Examples:**

```java
// member-service/service/MemberService.java:21
private static final Logger log = LoggerFactory.getLogger(MemberService.class);

// Usage:
log.debug("searchByKeyword - keyword: {}, pattern: {}", keyword, pattern);
log.debug("searchByUserTypeAndKeyword - userType: {}, keyword: {}, pattern: {}",
          userType, keyword, pattern);
```

### 8.2 System.out.println - **NOT USED** ✅

**Finding:** No `System.out.println` found in the codebase.

**Evidence:**
```bash
# Search results:
System\.out\.println
> Found 0 total occurrences across 0 files.
```

### 8.3 Logging Patterns

#### Pattern 1: Info level for business events

```java
log.info("[주문] 받은 요청 데이터 - requestMessage: {}, customerName: {}", requestMessage, customerName);
log.info("[주문] 주문 접수 완료 - orderId: {}, customerName: {}, status: PENDING", ...);
log.info("주문 상세 조회 요청 - ID: {}", orderId);
log.info("주문 내역 DTO 조회 요청 - 고객: {}, 기간: {}개월", customerId, period);
```

#### Pattern 2: Debug level for detailed tracing

```java
log.debug("searchByKeyword - keyword: {}, pattern: {}", keyword, pattern);
log.debug("searchByUserTypeAndKeyword - userType: {}, keyword: {}, pattern: {}",
          userType, keyword, pattern);
```

#### Pattern 3: Error level with exceptions

```java
log.error("[주문] 주문 실패: ", e);
log.error("Failed to publish order stock message: {}", e.getMessage(), e);
log.error("주문 상세 조회 중 오류: {}", e.getMessage());
log.error("주문 내역 조회 실패: {}", e.getMessage());
```

#### Pattern 4: e.printStackTrace() - **ANTI-PATTERN** ❌

Found extensively in controllers:

```java
// member-service/controller/AuthController.java:160
} catch (Exception e) {
    e.printStackTrace();  // ❌ Should use log.error()
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
    return ResponseEntity.status(500).body(error);
}

// board-service/controller/BoardController.java:74
} catch (Exception e) {
    e.printStackTrace();  // ❌ Should use log.error()
    return ResponseEntity.badRequest().body("게시글 작성에 실패했습니다: " + e.getMessage());
}
```

### 8.4 Logging Summary

| Method | Usage | Count | Issues |
|--------|-------|-------|--------|
| @Slf4j (Lombok) | ✅ Modern approach | 22 files | None |
| private static final Logger | ⚠️ Older approach | 2 files | Verbose |
| System.out.println | ✅ Not used | 0 files | N/A |
| e.printStackTrace() | ❌ Anti-pattern | ~69 occurrences | Should use logger |

**Best Practices:**
- ✅ No System.out.println usage
- ✅ @Slf4j used in newer services
- ✅ Structured logging with placeholders `{}`
- ❌ Extensive use of `e.printStackTrace()` instead of proper logging
- ⚠️ Inconsistent logging levels (some services have no logging at all)

---

## 9. Utility Classes

### 9.1 JwtUtil - **REPLICATED ACROSS SERVICES** ⚠️

**Location:**
```
member-service/util/JwtUtil.java
board-service/util/JwtUtil.java
admin-service/util/JwtUtil.java
```

**member-service/util/JwtUtil.java:**
```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration:3600000}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String userId, String role) { ... }
    public String generateRefreshToken(String userId) { ... }
    public String getUserIdFromToken(String token) { ... }
    public String getRoleFromToken(String token) { ... }
    public boolean validateAccessToken(String token) { ... }
    public boolean validateRefreshToken(String token) { ... }
    public void addToBlacklist(String token) { ... }

    // Backward compatibility methods
    public String generateToken(String userId, String role) { ... }
    public boolean validateToken(String token) { ... }
    public String getUsernameFromToken(String token) { ... }
    public String extractUserId(String token) { ... }
    public String extractRole(String token) { ... }
}
```

**Key Methods:**
- Token generation: `generateAccessToken()`, `generateRefreshToken()`
- Token validation: `validateAccessToken()`, `validateRefreshToken()`
- Token parsing: `getUserIdFromToken()`, `getRoleFromToken()`
- Blacklist management: `addToBlacklist()`

**Issue:** This class is **duplicated** across multiple services instead of being shared as a common library.

### 9.2 Other Utility Classes

**Found:**
- `ApiClient.java` - frontend-service/util/ApiClient.java (REST client wrapper)

**Not Found:**
- DateUtil ❌
- StringUtil ❌
- ValidationUtil ❌

### 9.3 Utility Classes Summary

| Utility Class | Services | Purpose | Issue |
|---------------|----------|---------|-------|
| JwtUtil | member, board, admin | JWT token management | ⚠️ Duplicated code |
| ApiClient | frontend | HTTP client wrapper | ✅ Service-specific |

**Observations:**
- **Very few utility classes** in the project
- Most utility logic is **embedded in services** (e.g., validation in controllers, masking in services)
- **No centralized validation** or common helpers
- **JwtUtil duplication** is a code smell - should be extracted to a shared library

---

## 10. Inconsistencies and Technical Debt

### 10.1 Package Naming Inconsistencies

```
❌ order-service uses "com.example.cust" instead of "com.example.order"
❌ board-service uses "com.example.boardservice" (concatenated)
❌ admin-service uses "com.du.adminservice" (different organization)
✅ member-service, product-service, inventory-service follow standard pattern
```

### 10.2 DTO Organization Inconsistencies

```
member-service:    dto/request/ and dto/response/
product-service:   dto/admin/ and dto/user/
order-service:     dto/ (flat)
inventory-service: dto/ with nested static classes
board-service:     No DTOs (uses inner classes in controllers)
```

### 10.3 Exception Handling Inconsistencies

```
❌ No global exception handler (@RestControllerAdvice)
❌ No custom exception classes
✅ Services consistently throw RuntimeException
❌ Controllers have duplicate try-catch logic
❌ Error response format varies by service
```

### 10.4 Response Format Inconsistencies

```
member-service:  Map with success/message/data/errorCode
order-service:   Map with custom fields per endpoint
product-service: Direct DTOs (no wrapper)
board-service:   Mix of Maps and plain strings
❌ ApiResponse<T> class exists but is unused
```

### 10.5 Dependency Injection Inconsistencies

```
✅ No @Autowired field injection (good)
⚠️ Mix of @RequiredArgsConstructor and manual constructors
❌ BCryptPasswordEncoder instantiated in constructor (not injected)
```

### 10.6 Logging Inconsistencies

```
✅ No System.out.println
⚠️ Mix of @Slf4j and Logger logger = ...
❌ Extensive use of e.printStackTrace() (should use log.error)
⚠️ Some services have no logging at all
```

### 10.7 Code Duplication

```
❌ JwtUtil duplicated in 3 services
❌ Validation logic duplicated in controllers
❌ Error response building duplicated in every endpoint
```

---

## 11. Recommendations for Improvement

### 11.1 High Priority

1. **Standardize package naming** - Use consistent pattern across all services
2. **Implement global exception handler** - Reduce controller code duplication
3. **Standardize API response format** - Use ApiResponse<T> or create new standard
4. **Replace e.printStackTrace()** with proper logging
5. **Extract JwtUtil to shared library** - Remove code duplication

### 11.2 Medium Priority

6. **Standardize DTO organization** - Choose one pattern (request/response or admin/user)
7. **Add custom exception classes** - Better error categorization
8. **Standardize @Transactional usage** - Add readOnly where appropriate
9. **Migrate to @RequiredArgsConstructor** - Remove manual constructors
10. **Add validation annotations** - Use @Valid and Bean Validation

### 11.3 Low Priority

11. **Add utility classes** - DateUtil, StringUtil, ValidationUtil
12. **Improve logging consistency** - Use @Slf4j everywhere
13. **Add API documentation** - Swagger/OpenAPI annotations
14. **Add unit tests** - Current coverage unknown

---

## 12. Quick Reference

### Entity to DTO Conversion (Best Practice)

```java
// ✅ Good - Builder pattern with Stream
return menus.map(menu -> MenuListDto.builder()
        .menuCode(menu.getMenuCode())
        .menuName(menu.getMenuName())
        .build());

// ❌ Bad - Manual mapping
MenuListDto dto = new MenuListDto();
dto.setMenuCode(menu.getMenuCode());
dto.setMenuName(menu.getMenuName());
```

### Error Response (Current Pattern)

```java
// Current pattern (inconsistent)
try {
    // business logic
} catch (Exception e) {
    e.printStackTrace();  // ❌ Bad
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("message", "오류 메시지");
    error.put("errorCode", "ERROR_CODE");
    return ResponseEntity.status(500).body(error);
}

// Recommended pattern
try {
    // business logic
} catch (Exception e) {
    log.error("Operation failed", e);  // ✅ Good
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("오류 메시지", "ERROR_CODE"));
}
```

### Dependency Injection (Recommended)

```java
// ✅ Good - @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
    // No constructor needed
}

// ⚠️ Acceptable - Manual constructor
@Service
public class MyService {
    private final MyRepository repository;

    public MyService(MyRepository repository) {
        this.repository = repository;
    }
}

// ❌ Bad - Field injection
@Service
public class MyService {
    @Autowired  // Avoid this
    private MyRepository repository;
}
```

### Transaction Management (Best Practice)

```java
// ✅ Good - Write operations
@Transactional
public void updateEntity(Entity entity) {
    repository.save(entity);
}

// ✅ Good - Read operations
@Transactional(readOnly = true)
public List<Entity> getComplexQuery() {
    return repository.findWithJoins();
}

// ✅ Good - Simple reads (no transaction needed)
public Entity findById(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Not found"));
}
```

---

## Appendix: File Locations Reference

### Controllers
```
member-service/controller/AuthController.java
member-service/controller/UserController.java
member-service/controller/AdminController.java
product-service/controller/MenuController.java
product-service/controller/OptionController.java
product-service/controller/admin/AdminProductController.java
order-service/controller/OrderController.java
order-service/controller/AdminController.java
board-service/controller/BoardController.java
board-service/controller/NoticeController.java
board-service/controller/CommentController.java
admin-service/controller/AdminAuthController.java
admin-service/controller/AdminUserController.java
admin-service/controller/AdminProductController.java
inventory-service/controller/InventoryController.java
inventory-service/controller/MaterialController.java
```

### Services
```
member-service/service/MemberService.java
member-service/service/EmailService.java
product-service/service/MenuService.java
product-service/service/OptionService.java
product-service/service/admin/AdminProductService.java
order-service/service/OrderService.java
order-service/service/CartDetailService.java
order-service/service/MakeCart.java
board-service/service/BoardService.java
board-service/service/NoticeService.java
inventory-service/service/InventoryService.java
inventory-service/service/InventoryServiceImpl.java
```

### Utilities
```
member-service/util/JwtUtil.java
board-service/util/JwtUtil.java
admin-service/util/JwtUtil.java
frontend-service/util/ApiClient.java
```

---

**Document Version:** 1.0
**Last Updated:** 2026-04-14
**Analyzed By:** Code Analysis Tool