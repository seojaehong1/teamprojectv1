# API Changelog

> **API 스펙 변경 이력. 코드 변경 시 자동으로 여기에 추가한다.**

---

## 기록 포맷

```markdown
## [버전] - YYYY-MM-DD

### Added
- 새로 추가된 API 엔드포인트
- 예: `POST /api/order/cancel` - 주문 취소 API 추가

### Changed
- 기존 API의 변경 사항 (요청/응답 스펙 변경)
- 예: `GET /api/products` - 응답에 `stockQuantity` 필드 추가

### Deprecated
- 더 이상 사용을 권장하지 않는 API (삭제 예정)
- 예: `GET /api/users/list` - v2에서 `/api/users`로 대체 예정

### Removed
- 삭제된 API
- 예: `DELETE /api/legacy/orders` - v1.5 이후 제거됨

### Fixed
- API 버그 수정
- 예: `POST /api/auth/login` - 비밀번호 5회 오류 시 잠금 로직 수정
```

---

## 변경 이력

## [Unreleased]

### Added
- `GET /api/system/info` - 서버 정보 조회 API (product-service)
  - 서비스명, 버전, Java 버전, Spring Boot 버전, 빌드 시간, 활성 프로파일, 포트 반환
  - 상세: `docs/changelog/current/001-system-info-api.md`

<!--
예시:

## [Unreleased]

### Added
- `POST /api/order/cancel` - 주문 취소 API 추가 (order-service)

### Changed
- `GET /api/products/{id}` - 응답에 `isAvailable` 필드 추가 (product-service)

---

## [1.0.0] - 2024-XX-XX

### Added
- 초기 API 릴리스
- member-service: `/api/auth/**`, `/api/users/**`
- product-service: `/api/products/**`, `/api/menu/**`
- order-service: `/api/order/**`
- inventory-service: `/api/inventory/**`
- board-service: `/api/boards/**`, `/api/notices/**`
- admin-service: `/api/admin/**`
-->