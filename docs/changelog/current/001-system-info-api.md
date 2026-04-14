# 변경사항: 서버 정보 API 추가

**날짜**: 2024-04-15
**서비스**: product-service
**타입**: 기능 추가 (Feature)

---

## 비개발자 요약

서버의 상태와 버전 정보를 확인할 수 있는 API가 추가되었습니다.
운영팀이나 모니터링 시스템에서 product-service의 현재 상태를 쉽게 확인할 수 있습니다.

**확인할 수 있는 정보**:
- 서비스 이름 및 버전
- Java 버전
- Spring Boot 버전
- 서버 시작 시간
- 현재 활성화된 환경 설정 (개발/운영 등)
- 서비스 포트 번호

---

## 기술 상세

### API 엔드포인트

**요청**:
```
GET /api/system/info
```

**응답 예시**:
```json
{
  "success": true,
  "message": "서버 정보 조회 성공",
  "data": {
    "serviceName": "product-service",
    "version": "1.0.0",
    "javaVersion": "17.0.8",
    "springBootVersion": "3.1.5",
    "buildTime": "2024-04-15 10:30:00",
    "activeProfile": "default",
    "serverPort": 8001
  }
}
```

### 변경된 파일

| 파일 | 변경 타입 | 설명 |
|------|----------|------|
| `product-service/src/main/java/com/example/product/controller/SystemController.java` | 추가 | 시스템 정보 API 컨트롤러 |
| `product-service/src/main/java/com/example/product/dto/system/SystemInfoDto.java` | 추가 | 시스템 정보 응답 DTO |
| `product-service/src/test/java/com/example/product/controller/SystemControllerTest.java` | 추가 | 단위 테스트 |

### 테스트 케이스

1. **서버 정보 조회 성공**: 모든 필드가 올바르게 반환되는지 확인
2. **Java 버전 형식 확인**: 버전 문자열이 숫자로 시작하는지 확인
3. **빌드 시간 형식 확인**: `yyyy-MM-dd HH:mm:ss` 형식인지 확인

### 의존성

- 새로운 외부 라이브러리 추가 없음
- 기존 Spring Boot 의존성만 사용

### Gateway 라우팅

기존 product-service 라우팅 (`/api/**`)에 포함되므로 별도 설정 불필요:
```
GET http://gateway:8000/api/system/info → product-service:8001
```

---

## 롤백 방법

해당 파일 3개를 삭제하면 원복됩니다:
```bash
rm product-service/src/main/java/com/example/product/controller/SystemController.java
rm product-service/src/main/java/com/example/product/dto/system/SystemInfoDto.java
rm product-service/src/test/java/com/example/product/controller/SystemControllerTest.java
```