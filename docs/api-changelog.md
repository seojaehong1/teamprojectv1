# API Changelog

API 변경 이력을 기록합니다.

---

## [Unreleased]

### Added
- **GET /api/system/info** (product-service): 서버 정보 조회 API
  - 서비스명, 버전, Java 버전, 활성 프로파일, 포트 정보 반환
  - 응답 형식: `{ success, message, data }`