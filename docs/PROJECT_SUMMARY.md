# 프로젝트 불필요한 파일 분석 보고서

## 📅 분석 일자: 2025-12-31

## 📊 분석 결과 요약

### ✅ 이미 정리 완료
| 항목 | 상태 | 절약 용량 |
|------|------|----------|
| 모든 서비스 `build/` 폴더 | ✅ 삭제됨 (gradlew clean) | ~845MB |
| 모든 서비스 `bin/` 폴더 | ✅ 삭제됨 | ~10MB |
| 중복 `DataInitializer.java` | ✅ 삭제됨 | - |
| 루트 `data/` 폴더 | ✅ 삭제됨 | ~40KB |
| 잘못된 테스트 폴더들 | ✅ 삭제됨 | - |

**결과: 900MB → 약 134MB (약 766MB 절약)**

---

## ✅ 추가 정리 완료 항목

### 1. ~~중복된 초기화 클래스 (member-service)~~ ✅ 삭제됨

**문제:** 동일한 기능을 하는 두 개의 초기화 클래스가 존재
- ~~`member-service/src/main/java/com/example/member/config/DataInitializer.java`~~ (삭제됨)
- `member-service/src/main/java/com/example/member/config/DataLoader.java` (유지)

### 2. ~~중복된 데이터베이스 파일~~ ✅ 삭제됨

**문제:** 동일한 DB 파일이 두 위치에 존재
- ~~`data/memberdb.mv.db` (루트 폴더)~~ (삭제됨)
- `member-service/data/memberdb.mv.db` (유지)

### 3. ~~잘못된 패키지 구조의 테스트 파일 (board-service)~~ ✅ 삭제됨

**문제:** 테스트 클래스의 패키지가 메인 코드와 불일치
- ~~`board-service/src/test/java/com/du/boardservice/BoardServiceApplicationTests.java`~~ (삭제됨)

### 4. ~~빈 테스트 폴더 (admin-service)~~ ✅ 삭제됨

~~위치: `admin-service/src/test/java/com/du/`~~ (삭제됨)

---

## 📁 현재 프로젝트 구조 (정리 후)

```
teamprojectv1_3/
├── admin-service/      # 관리자 서비스
├── board-service/      # 게시판 서비스
├── cust-service/       # 고객 서비스
├── eureka-server/      # 서비스 디스커버리
├── frontend-service/   # 프론트엔드 (Thymeleaf)
├── gateway-service/    # API 게이트웨이
├── inventory-service/  # 재고 서비스
├── member-service/     # 회원 서비스
├── order-service/      # 주문 서비스
├── product-service/    # 상품 서비스
├── docs/               # 문서
└── gradle/             # Gradle 래퍼
```

---

## 🔧 정리 명령어

### build 폴더 정리 (완료됨)
```cmd
gradlew clean
```

### bin 폴더 정리 (완료됨)
```cmd
for /d %d in (*-service eureka-server) do @if exist "%d\bin" rd /s /q "%d\bin"
```

### 중복 DB 폴더 삭제 (완료됨)
```cmd
rd /s /q data
```

### .gitignore에 추가 권장 항목
```
# Build outputs
**/build/
**/bin/

# IDE
.idea/
*.iml

# Gradle
.gradle/

# Database files (로컬 개발용)
**/data/*.db
```

---

## 📋 서비스별 주요 코드 파일

### member-service
- `MemberApplication.java` - 메인 애플리케이션
- `AuthController.java` - 인증 API
- `UserController.java` - 사용자 API
- `MemberService.java` - 회원 비즈니스 로직
- `DataLoader.java` - 초기 데이터 로딩

### board-service
- `BoardController.java` - 게시판 API
- `NoticeController.java` - 공지사항 API
- `CommentController.java` - 댓글 API
- `DataLoader.java` - 초기 공지사항 데이터

### admin-service
- `AdminAuthController.java` - 관리자 인증
- `AdminUserController.java` - 사용자 관리
- `AdminInquiryController.java` - 문의 관리 (관리자용)
- `InquiryController.java` - 문의 API (일반 사용자용)
- `TestDataInitializer.java` - 테스트 문의 데이터
